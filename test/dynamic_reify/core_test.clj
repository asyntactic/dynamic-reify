(ns dynamic-reify.core-test
  (:use clojure.test
        dynamic-reify.core))

(deftest test-make-protos
  (is (= (make-protos '(foo
                        ((f1 [this]) identity)
                        ((f2 [this that]) identity)
                        bar
                        ((f3 [this]) identity)))
         [['foo
           ['f1 ['this]]
           identity
           ['f2 ['this 'that]]
           identity]
          ['bar ['f3 ['this]] identity]])))

(deftest test-make-protos-ommissions
  (is (= [['bar ['f3 ['this]] identity]]
         (make-protos '(foo
                        ((f1 [this]) identity)
                        ((f2 [this that]) nil)
                        bar
                        ((f3 [this]) identity))))))
         

(defprotocol foo (f1 [this]))
(defprotocol bar (f2 [this]))

(deftest simple-dynamic-reification
  (is (= [1 2]
         (let [i (dynamic-reify (foo
                                 ((f1 [this]) (fn [x] 1))
                                 bar
                                 ((f2 [this]) (fn [x] 2))))]
           [(.f1 i) (.f2 i)]))))


