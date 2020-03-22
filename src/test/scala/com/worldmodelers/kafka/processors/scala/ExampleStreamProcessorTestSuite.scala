package com.worldmodelers.kafka.processors.scala

import java.util.Properties

import better.files.Resource
import com.worldmodelers.kafka.messages.{ExampleStreamMessage, ExampleStreamMessageJsonFormat, ExampleStreamMessageSerde}
import net.manub.embeddedkafka.ConsumerExtensions._
import net.manub.embeddedkafka.EmbeddedKafkaConfig
import net.manub.embeddedkafka.streams.EmbeddedKafkaStreamsAllInOne
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.scalatest.{FlatSpec, Matchers}

class ExampleStreamProcessorTestSuite extends FlatSpec with Matchers with EmbeddedKafkaStreamsAllInOne with ExampleStreamMessageJsonFormat {

    private val properties : Properties = {
        val propStream = Resource.getAsStream( s"test.properties" )
        val p = new Properties()
        p.load( propStream )
        p
    }

    implicit val stringSerdes = new StringSerde
    implicit val stringSerializer = stringSerdes.serializer
    implicit val stringDeserializer = stringSerdes.deserializer

    implicit val messageSerde : ExampleStreamMessageSerde = new ExampleStreamMessageSerde
    implicit val messageSerializer = messageSerde.serializer()
    implicit val messageDeserializer = messageSerde.deserializer()

    implicit val config = EmbeddedKafkaConfig( kafkaPort = 6308, zooKeeperPort = 2111 )

    "Example Stream Processor" should "process a message" in {
        val streamProcessor = new ExampleStreamProcessor( properties )
        val inputTopic = "stream.in"
        val outputTopic = "stream.out"

        val streamMessage = ExampleStreamMessage( "id1" )
        val streamMessageJson = marshalMessage( streamMessage ).get

        runStreams( streamProcessor.topics(), streamProcessor.buildStream() ) {
            publishToKafka( inputTopic, streamMessage.id, streamMessageJson )
                withConsumer[ String, ExampleStreamMessage, Unit ] { consumer =>
                    val messages : Stream[ ConsumerRecord[ String, ExampleStreamMessage ] ] = consumer.consumeLazily( outputTopic )
                    val actual = messages.head
                    actual.value.breadcrumbs should contain ("scala-kafka-streams")
                }
        }
    }

}