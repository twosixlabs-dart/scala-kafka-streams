package com.worldmodelers.kafka.messages

import java.util.{Map => JMap}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.kafka.common.serialization.Serdes.WrapperSerde
import org.apache.kafka.common.serialization.{Deserializer, Serializer}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}

trait ExampleStreamMessageJsonFormat {

    private val LOG : Logger = LoggerFactory.getLogger( getClass )

    val mapper : ObjectMapper = {
        val m = new ObjectMapper()
        m.registerModule( DefaultScalaModule )
    }

    def unmarshalMessage( json : String ) : Try[ ExampleStreamMessage ] = {
        try {
            Success( mapper.readValue[ ExampleStreamMessage ]( json, classOf[ ExampleStreamMessage ] ) )
        } catch {
            case e : Exception => {
                LOG.error( s"Encountered error unmarshalling json : ${e.getClass} : ${e.getMessage} : ${e.getCause}" )
                e.printStackTrace()
                Failure( e )
            }
        }
    }

    def marshalMessage( message : ExampleStreamMessage ) : Try[ String ] = {
        try {
            Success( mapper.writeValueAsString( message ) )
        } catch {
            case e : Exception => {
                LOG.error( s"Encountered error marshalling json : ${e.getClass} : ${e.getMessage} : ${e.getCause}" )
                e.printStackTrace()
                Failure( e )
            }
        }
    }

}

class ExampleStreamMessageSerializer extends Serializer[ ExampleStreamMessage ] with ExampleStreamMessageJsonFormat {

    override def configure( configs : JMap[ String, _ ], isKey : Boolean ) : Unit = {}

    override def serialize( topic : String, data : ExampleStreamMessage ) : Array[ Byte ] = marshalMessage( data ).get.getBytes

    override def close( ) : Unit = {}
}


class ExampleStreamMessageDeserializer extends Deserializer[ ExampleStreamMessage ] with ExampleStreamMessageJsonFormat {

    override def configure( configs : JMap[ String, _ ], isKey : Boolean ) : Unit = {}

    override def deserialize( topic : String, data : Array[ Byte ] ) : ExampleStreamMessage = unmarshalMessage( new String( data ) ).get

    override def close( ) : Unit = {}
}

class ExampleStreamMessageSerde extends WrapperSerde[ ExampleStreamMessage ]( new ExampleStreamMessageSerializer, new ExampleStreamMessageDeserializer )
