package akka.sample.cluster.kubernetes

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property = "type")
trait SerializableMessage
