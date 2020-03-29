(ns punt.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.string :as s]
            [clojure.walk :as w])
  (:gen-class))


(set! *warn-on-reflection* true)

(defn seperate-files-folders [coll node]
  (if (map? node)
    (assoc coll :files (conj (or (:files coll) []) node))
    (assoc coll :folders (conj (or (:folders coll) []) node))))

(defn squash-down [root folders]
  (let [name-fn #(str (name root) "/" (name (first %)))]
    (map #(apply vector (name-fn %) (rest %)) folders)))

(defn filter-files [state root {fls :files flds :folders}]
  (let [dir-path #(str %1 "/" %2)
        add-file-paths
        (fn [files]
          (doall
           (map #(swap! state assoc (dir-path (name root) (:name %)) (:value %))
                files)))]
    (add-file-paths fls)
    (if (empty? flds)
      (when (empty? fls)
        (do (swap! state assoc (str (name root) "/.") nil)
            nil))
      (squash-down root flds))))

(defn work [state node]
    (cond (keyword? node)   (name node)
          (vector? node)    (->> (reduce seperate-files-folders {} (rest node))
                                 (filter-files state (first node)))
          (empty? node)      nil 
          (map-entry? node) node
          :else             node))

(defn extract-file-layout [structure]
  (let [file-paths (atom {})
        _ (w/prewalk #(work file-paths %) structure)]
    @file-paths))

(defn prepend-destination [destination collection]
  (reduce (fn [c [k v]]
            (assoc c (str destination k) v))
          {}
          collection))

(defn genereate-project-directory [file-map]
  (doall
   (map
    (fn [[file-path content]]
      (if (nil? content)
        (io/make-parents file-path)
        (do (io/make-parents file-path)
            (spit file-path content))))
    file-map)))

(defn default-source [& args]
  (let [path (System/getenv "PUNT_PATH")
        home (System/getenv "HOME")]
    (if path
      path
      (str home "/.local/share/punter"))))

(def cli-options
  [["-s" "--source SOURCE" "Source Directory"
    :id :source
    :default-fn default-source
    :default-desc "$HOME/.local/share/punter"]
   ["-d" "--destination DESTINATION" "Template Destination"
    :id :destination
    :default "./"
    :default-desc "~/other/than/here"]
   ["-h" "--help" ]])

(defn tool-description [summary]
  (str "\nPunt is a edn based project boilerplate tool.\n\tIt takes an edn template description and generates a project\n\n"
       summary
       "\n"))

(defn error-message [errors]
  (let [error-log (apply str (map #(str "\t" % "\n") errors))
        message-fn #(str "Some errors to be concerned about: \n\n"
                         %
                         "\nCall `punt` or `punt -h` to see how to use the tool\n")]
    (message-fn error-log)))

(defn extract-file [source-dir]
  (fn [file-path]
    (let [content (slurp (str source-dir file-path))
          file-name (last (s/split (str file-path) #"/"))]
      {:name file-name :value content})))

(defn local-reader [source-dir]
  {:readers {'punt/file (extract-file  source-dir)}})

(defn local-punter [{:keys [destination source]} [template]]
  (let [template-folder (str source "/" template "/")
        file-path (str template-folder "def.edn")]
    (try
      (->> (slurp file-path)
           (edn/read-string (local-reader template-folder))
           :layout
           (extract-file-layout)
           (prepend-destination destination)
           (genereate-project-directory))

      (catch java.io.FileNotFoundException e
        (printf "Attempted to read local resource:\n\t%s\n" (.getMessage e))))))

(defn -main [& args]
  (let [{:keys [options arguments summary errors]} (parse-opts args cli-options)]
    (cond (:help options) (println (tool-description summary))
          errors (println (error-message errors))
          (empty? arguments) (println (tool-description summary))
          :else (local-punter options arguments))))

