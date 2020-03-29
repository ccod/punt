install:
	lein deps
	lein do clean, uberjar
	lein native
	cp target/punt ~/.local/bin/
