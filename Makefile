all:
	javac *.java
cl:
	rm -f *.class
run:
	java p1
test:
	perl difftest.pl -1 "rpal/rpal -ast -noout FILE" -2 "java p1 -ast -noout FILE" -t rpal/tests/