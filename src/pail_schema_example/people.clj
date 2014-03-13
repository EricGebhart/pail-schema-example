(ns pail-schema-example.people
  (:require [schema.core :as s]))

; a location structure.
(def Location
  "A schema for a location"
    {:address (s/maybe s/Str)
     :city    (s/maybe s/Str)
     :county  (s/maybe s/Str)
     :state   (s/maybe s/Str)
     :country (s/maybe s/Str)
     :zip     (s/maybe s/Str)})
                                        ; the basic union of properties
(def PersonProperties
  "A Union of possible properties"
     (s/either
       {:first-name s/Str}
       {:last-name s/Str}
       {:location Location}
       {:age s/Int}))

(def PersonProperty
  "A Person property "
    { :id s/Str  ; make this s/Uuid
      :property PersonProperties})

; an Edge.
(def FriendshipEdge
  "A schema for a friendship edge connector."
   {(s/required-key :id1) s/Str
    (s/required-key :id2) s/Str})

(def DataUnit
  "The basic DataUnit for the database."
    (s/either
      {(s/required-key :person-property) PersonProperty}
      {(s/required-key :friendshipedge) FriendshipEdge}))

(defn master-schema
  "Return the master schema"
  [] DataUnit)

(defn person-property [id property]
  ^{:type ::PersonProperty}
  {:id id :property property})

(defn first-name [name]
  ^{:type ::FirstName}
  {:first-name name})

(defn last-name [name]
  ^{:type ::LastName}
  {:last-name name})

(defn age [name]
  ^{:type ::Age}
  {:age name})

(defn location [{:keys [address city county state country zip]}]
  ^{:type ::Location}
  {:location {:address address :city city :county county :state state :country country :zip zip}})

(defn friendshipedge [id1 id2]
  ^{:type ::friendshipEdge}
   {:id1 id1 :id2 id2})

(defn dataunit [key property]
  ^{:type ::DataUnit}
  {key property})

;helpers to build Data Units.
(defn create-person-property [id property]
  (dataunit :person-property (person-property id property)))

(defn create-friendshipedge [id1 id2]
  (dataunit :friendshipedge (friendshipedge id1 id2)))
