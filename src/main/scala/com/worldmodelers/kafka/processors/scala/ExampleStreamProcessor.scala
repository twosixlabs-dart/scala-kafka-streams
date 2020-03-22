package com.worldmodelers.kafka.processors.scala

import java.time.Duration
import java.util.Properties

import com.worldmodelers.kafka.messages.{ExampleStreamMessage, ExampleStreamMessageSerde}
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.kstream.{Consumed, Produced}
import org.apache.kafka.streams.scala.{Serdes, StreamsBuilder}
import org.apache.kafka.streams.{KafkaStreams, Topology}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

class ExampleStreamProcessor( val properties : Properties ) {
    private val LOG : Logger = LoggerFactory.getLogger( getClass )
    private val kafkaProps : Properties = getKafkaProperties()

    val stringSerdes : Serde[ String ] = Serdes.String
    val streamMessageSerdes : Serde[ ExampleStreamMessage ] = new ExampleStreamMessageSerde

    implicit val consumed : Consumed[ String, ExampleStreamMessage ] = Consumed.`with`( stringSerdes, streamMessageSerdes )
    implicit val produced : Produced[ String, ExampleStreamMessage ] = Produced.`with`( stringSerdes, streamMessageSerdes )

    def buildStream( ) : Topology = {

        val builder = new StreamsBuilder
        builder.stream( "stream.in" ).mapValues( message => {
            message.copy( breadcrumbs = message.breadcrumbs :+ "scala-kafka-streams" )
        } ).to( "stream.out" )

        builder.build( kafkaProps )
    }

    def topics( ) : Seq[ String ] = Seq( "stream.in", "stream.out" )

    def runStreams( ) : Unit = {
        val topology = buildStream()
        val streams = new KafkaStreams( topology, kafkaProps )

        streams.start()
        sys.addShutdownHook( streams.close( Duration.ofSeconds( 10 ) ) )
    }

    private def getKafkaProperties( ) : Properties = {
        val kProps = properties
          .asScala
          .toList
          .filter( _._1.startsWith( "kafka." ) )
          .map( pair => {
              val key = pair._1.split( "kafka." )( 1 )
              (key, pair._2)
          } )

        val kafkaProps = new Properties()
        kProps.foreach( prop => kafkaProps.put( prop._1, prop._2 ) )
        kafkaProps
    }

}