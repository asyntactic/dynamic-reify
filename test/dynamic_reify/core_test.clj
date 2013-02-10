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
         
(deftest make-protos-conditional-ommissions
  (is (= [['foo ['f1 ['this]] identity]]
         (make-protos '(foo
                        ((f1 [this]) identity)
                        bar
                        ((f2 [this]) (if (= 0 1)
                                       (fn [x] 2))))))))


(defprotocol foo (f1 [this]))
(defprotocol bar (f2 [this]))

(deftest simple-dynamic-reification
  (is (= [1 2]
         (let [i (dynamic-reify (foo
                                 ((f1 [this]) (fn [x] 1))
                                 bar
                                 ((f2 [this]) (fn [x] 2))))]
           [(.f1 i) (.f2 i)]))))


(deftest omitted-protocol
  (let [i (dynamic-reify (foo
                          ((f1 [this]) (fn [x] 1))
                          bar
                          ((f2 [this]) (if (= 0 1)
                                         (fn [x] 2)))))]
    (is (= 1 (.f1 i)))
    (is (thrown? IllegalArgumentException
                 (.f2 i)))))


(let [x :closed-value]
  (defn a-closure-var [this]
    x))

(deftest defned-closure
  (is (= :closed-value
         (let [i (dynamic-reify (foo
                                 ((f1 [this]) a-clojure-var)))]
           (.f1 i)))))

(deftest lexical-closure
  (is (= :closed-value
         (let [x :closed-value
               i (dynamic-reify (foo
                                 ((f1 [this]) (fn [t] x))))]
           (.f1 i)))))

(deftest conditional-reification
  (is (= 5
         (let [n 2
               i (dynamic-reify (foo
                                 ((f1 [this that]) (if (> n 3)
                                                     (fn [t x] :greater)
                                                     (fn [t x] x)))))]
           (.f1 i)))))


