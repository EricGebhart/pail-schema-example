(ns pail-schema-example.data-unit-pail-structure
  (:require [clj-pail-tap.structure :refer [gen-structure]]
            [pail-fressian.serializer :as s]
            [pail-schema.partitioner :as p]
            [pail-schema.core :as pc]
            [pail-schema.tapmapper :as t]
            [pail-schema-example.people :as people])
  (:gen-class))

(gen-structure pail-schema-example.DataUnitPailStructure
               :schema (people/master-schema)
               :serializer  (s/fressian-serializer)
               :partitioner (p/property-name-partitioner (people/master-schema))
               :tapmapper   (t/property-name-tap-mapper)
               :property-path-generator pc/property-paths)
