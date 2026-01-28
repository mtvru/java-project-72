run-dist:
	./build/install/app/bin/app $(ARGS)

install-dist:
	./gradlew clean installDist

build:
	./gradlew clean build

test:
	./gradlew clean test

run:
	DATABASE_URL="jdbc:h2:mem:page_analyzer;DB_CLOSE_DELAY=-1" ./gradlew run

report:
	./gradlew jacocoTestReport

check:
	./gradlew clean check

.PHONY: build
