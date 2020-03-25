import java.util.Properties

import better.files.Resource
import com.worldmodelers.kafka.processors.scala.ExampleStreamProcessor

object Main {

    def main( args : Array[ String ] ) : Unit = {
        val properties : Properties = {
            val p = new Properties()
            val pis = Resource.getAsStream( f"${args( 0 )}.properties" )
            p.load( pis )
            p
        }

        val streamProcessor = new ExampleStreamProcessor( properties )
        streamProcessor.runStreams()
    }

}