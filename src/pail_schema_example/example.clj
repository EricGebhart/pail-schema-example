(ns pail-schema-example.example
  (:require
   [pail-schema.core :as pail]
   [schema.core :as s]
   [schema.coerce :as coerce]
   [pail-schema-example.people :as p])
  (:use cascalog.api)
  (:import [pail-schema-example DataUnitPailStructure]))

;data

(def du1-1 (p/create-person-property "123" (p/first-name "Eric")))
(def du1-2 (p/create-person-property "123" (p/last-name "Gebhart")))
(def du1-3 (p/create-person-property "123" (p/location {:address "1 Pack Place"
                                                                       :city "Asheville"
                                                                       :state "NC"})))
(def du1-4 (p/create-person-property "123" (p/age "40")))
(def du1-5 (p/create-person-property "123" (p/age 50)))
(def du2-1 (p/create-person-property "456" (p/first-name "Frederick")))
(def du2-2 (p/create-person-property "456" (p/last-name "Gebhart")))
(def du2-3 (p/create-person-property "456" (p/location {:address "1 Wall Street"
                                                                       :city "Asheville"
                                                                       :state "NC"})))
(def du3 (p/create-friendshipedge "123" "456"))

(def objectlist [du1-1 du1-2 du1-3 du1-5 du2-1 du2-2 du2-3 du3])

(map #(s/validate %1 %2) (repeat (p/master-schema)) objectlist)

(def jcoerce-dataunit
  (coerce/coercer (p/master-schema) coerce/json-coercion-matcher))

(def coerce-dataunit
  (coerce/coercer (p/master-schema) coerce/string-coercion-matcher))

;this should fail
;(s/validate (p/master-schema) du1-4)

;get a good version of du1-4
(def du1-4c (coerce-dataunit du1-4))

(def objectlist (conj objectlist du1-4c))

(defmapfn pprop [{:keys [person-property]}]
    "Deconstruct a property object"
    (into [(:id person-property)] (:property person-property)))

(defmapfn sprop [{:keys [person-property]}]
    "Deconstruct a property object"
    (into [(:id person-property)] (vals (:property person-property))))

(defmapfn locprop [{:keys [person-property]}]
    "Deconstruct a location property object"
    (into [(:id person-property)] (vals (:location (:property person-property)))))

(comment
  defn to-json [keys]
  (mapfn [& args]
         (json/generate-string (zipmap keys args)))

  ((to-json ['foo' 'bar']) ?v1 ?v2 :> ?json))


(def mypail (pail/find-or-create ( DataUnitPailStructure.) "example_output"))

(defn prop-tap
  "Get a tap for all the person property partitions in one."
  [pail-connection]
  (pail/pail->tap pail-connection
                  :attributes (map #(%1 %2)
                                   [:first-name :last-name :location :age]
                                   (repeat (pail/tap-map mypail)))))

(defn raw-tap [pail-connection] (pail/pail->tap pail-connection))

(defn raw-query [pail-connection]
  (let [ptap (raw-tap pail-connection)]
    (??<- [?data] (ptap _ ?data))))

(defn personprop-query [pail-connection]
  (let [ptap (prop-tap pail-connection)]
    (??<- [?id ?property]
          (ptap _ ?data)
          (pprop ?data :> ?id ?property))))

(defn loc-prop-query [pail-connection]
  (let [ptap (pail/get-tap pail-connection :location)]
    (??<- [?id ?property]
          (ptap _ ?data)
          (pprop ?data :> ?id ?property))))


(defn get-names [pail-connection]
  (let [fntap (pail/get-tap pail-connection :first-name)]
    (??<- [?id ?first-name]
          (fntap _ ?fn-data)
          (sprop ?fn-data :> ?id ?first-name))))


(defn get-full-names [pail-connection]
  (let [fntap (pail/get-tap pail-connection :first-name)
        lntap (pail/get-tap pail-connection :last-name)]
    (??<- [?first-name ?last-name]
          (fntap _ ?fn-data)
          (lntap _ ?ln-data)
          (sprop ?fn-data :> ?id ?first-name)
          (sprop ?ln-data :> ?id ?last-name))))

(defn get-location [pail-connection]
  (let [loctap (pail/get-tap pail-connection :location)]
    (??<- [!address !city !county !state !country !zip]
          (loctap _ ?loc-data)
          (locprop ?loc-data :> ?id !address !city !county !state !country !zip))))

(defn get-slocation [pail-connection]
  (let [loctap (pail/get-tap pail-connection :location)]
    (??<- [?id ?data]
          (loctap _ ?loc-data)
          (sprop ?loc-data :> ?id !data))))

(defn get-everything [pail-connection]
  (let [fntap (pail/get-tap pail-connection :first-name)
        lntap (pail/get-tap pail-connection :last-name)
        loctap (pail/get-tap pail-connection :location)]
    (??<- [?first-name ?last-name !address !city !county !state !country !zip]
          (fntap _ ?fn-data)
          (lntap _ ?ln-data)
          (loctap _ ?loc-data)
          (agetap _ ?age-data)
          (sprop ?fn-data :> ?id ?first-name)
          (sprop ?ln-data :> ?id ?last-name)
          (locprop ?loc-data :> ?id !address !city !county !state !country !zip))))


(defn tests []
  (let [pail-struct (DataUnitPailStructure.)]
                                        ; see which partitioner we have
    (println (.getPartitioner pail-struct))
    (println (.getTapMapper pail-struct))
                                        ; print target partitions
    (prn-str [
              (.getTarget pail-struct du1-1)
              (.getTarget pail-struct du1-2)
              (.getTarget pail-struct du1-3)
              (.getTarget pail-struct du3)
              ])
    )
  (let [pc (pail/find-or-create (DataUnitPailStructure.) "example_output")
        fntap (pail/get-tap pc :first-name)
        loctap (pail/get-tap pc :location)]
                                        ;print objects and their deconstructed values
    (println du1-1)
    (println (sprop du1-1))
    (println du1-2)
    (println (sprop du1-2))
    (println du1-3)
    (println (sprop du1-3))

                                        ; write the objects to the pail
    (pail/write-objects pc objectlist)

                                        ; Query the data back out.
    (def names (??<- [?id ?first-name]
                     (fntap _ ?fn-data)
                     (sprop ?fn-data :> ?id ?first-name)))

    (def locs (??<- [?id !address !city !county !state !country !zip]
                    (loctap _ ?loc-data)
                    (locprop ?loc-data :> ?id !address !city !county !state !country !zip)))

    (println "Names===========================")
    (println names)
    (println "Locations===========================")
    (println locs)


    ))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (println "Hello, World!")
  (tests))
