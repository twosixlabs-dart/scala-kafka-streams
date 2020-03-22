package com.worldmodelers.kafka.messages

import com.fasterxml.jackson.annotation.JsonProperty

case class ExampleStreamMessage( @JsonProperty( "id" )
                                 id : String,
                                 @JsonProperty( "breadcrumbs" )
                                 breadcrumbs : Seq[ String ] = Seq() )