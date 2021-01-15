Ao descarregar a biblioteca java-future-jdk8 a partir do maven, reparamos que esta se encontrava desatualizada em relação ao repositório no GitHub, por isso descarregamos a biblioteca a partir de lá e colocámos na pasta src/lib/java/. Ao utilizar o IntelliJ é possível que seja necessário acrescentar esta pasta às pastas de source.

===================================

> Para correr os servidores:

Correr 4 distributedmap.servers.Server com os seguintes argumentos:
-n 0
-n 1
-n 2
-n 3

Correr 1 distributedmap.servers.ClockServer

> Posteriormente, pode-se correr as seguintes classes:

distributedmap.benchmark.Benchmark
test/java/ConcurrentTest
test/java/InteractiveClient
