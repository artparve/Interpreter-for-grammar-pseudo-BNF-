# Interpreter for grammar (pseudo BNF)

Books:
 - Design of compilers: A tutorial. Sergey Sverdlov. LAP LAMBERT Academic Publishing (https://comsys.kpi.ua/katalog/files/konstruyuvannya-kompilyatoriv.pdf)
 - Compilers: Principles, Techniques, and Tools. 	Pearson Education, Inc. (https://www.amazon.com/Compilers-Principles-Techniques-Tools-2nd/dp/0321486811)

Plan for Future:
 - Restore order in Parser types to get rid of frequent explicit conversions of numbers "Any -> Double"
 - Rewrite MyGrammar and MyGrammarSimple objects into classes so that inheritance can be used, thereby reducing duplicate code.
 - Some Updates are needed to improve error output