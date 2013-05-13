(ns adi.test-core
 (:use midje.sweet
       adi.utils
       adi.schema
       adi.data
       adi.checkers)
 (:require [datomic.api :as d]
           [adi.core :as adi]))

(def ^:dynamic *ds* nil)

(defn reset-database []
  (def ^:dynamic *ds*
    (adi/datastore "datomic:mem://adi-core-test-basics"
               {:account {:id   [{:type :long}]
                          :name [{:type :string}]
                          :tags [{:type :string
                                  :cardinality :many}]}}
               true true))

  (adi/insert! [{:account {:id 0 :name "chris" :tags #{"g1" "sun"  "boo"}}}
                {:account {:id 1 :name "dave"  :tags #{"g1" "sun"  "boo"}}}
                {:account {:id 2 :name "chris" :tags #{"g2" "sun"  "boo"}}}
                {:account {:id 3 :name "dave"  :tags #{"g2" "sun"  "boo"}}}
                {:account {:id 4 :name "chris" :tags #{"g1" "moon" "boo"}}}
                {:account {:id 5 :name "dave"  :tags #{"g1" "moon" "boo"}}}
                {:account {:id 6 :name "chris" :tags #{"g2" "moon" "boo"}}}
                {:account {:id 7 :name "dave"  :tags #{"g2" "moon" "boo"}}}]
               *ds*))


(reset-database)

(fact
  (adi/select {:account/id #{(? > 2) (? < 6)}} *ds*)
  => (contains-in [{:account {:tags #{"sun" "boo" "g2"}, :id 3, :name "dave"}}
                   {:account {:tags #{"moon" "boo" "g1"}, :id 4, :name "chris"}}
                   {:account {:tags #{"moon" "boo" "g1"}, :id 5, :name "dave"}}]
                  :in-any-order)

  (adi/select {:account/id #{(? > 2) (? < 6)}} *ds* :view {:account/tags :hide})
  => (just-in [{:account {:id 3, :name "dave"}, :db anything}
               {:account {:id 4, :name "chris"}, :db anything}
               {:account {:id 5, :name "dave"}, :db anything}]
              :in-any-order)

  (adi/select {:account/tags #{"g1" "moon"}} *ds* :view {:account/tags :hide})
  => (just-in [{:account {:id 4, :name "chris"} :db anything}
               {:account {:id 5, :name "dave"} :db anything}]
              :in-any-order))