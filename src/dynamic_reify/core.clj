(ns dynamic-reify.core)

;; make-protos translates the defs structure into a the structure used
;; to create the reification in dynamic-reify. make-protos DOES NOT
;; eval function forms and remove nil ones - calling eval would remove the 
;; forms from lexical scope. evaluation happens implicitly in the expansion 
;; of dynamic-reify 
(defn make-protos [defs]
  (loop [results [] remaining defs]
    (if (empty? remaining)
      results
      (let [proto (first remaining)
            [funs remain] (split-with (complement symbol?)
                                      (rest remaining))
            evalled-funs (mapcat (fn [[fun-args form]]
                                   `['~(vec fun-args) ~form])
                                 funs)]
        (recur (conj results (vec (concat `['~proto] evalled-funs)))
               remain)))))

(defmacro dynamic-reify [defs]
  (let [protosg# (vary-meta (gensym) merge {:dynamic true})]
    `(do
       (def ~protosg#)
       ;; function form evaluation happens implicitly...
       (binding [~protosg# ~(make-protos defs)] ;; <- evalled here
         ;;(println ~protosg#)
         (let [reification# 
               (apply concat 
                      (map-indexed
                       (fn [idx# [proto# & funs#]] 
                         (concat [proto#]
                                 (map (fn [[[name# args#] func#]]
                                        (list name# args# 
                                              `(apply (get-in ~'~protosg# 
                                                              [~idx# 0 2])
                                                      ~args#)))
                                      (partition 2 funs#))))
                       ;; omit protos with nil funs
                       (filter (complement (partial some nil?))
                               ~protosg#)))]
           (println reification#)
           #_(clojure.pprint/pprint `(let [~'~protosg# ~'~protosg#]
                                     (reify ~@reification#)))
           (eval `(let [~'~protosg# ~'~protosg#]
                    (clojure.pprint/pprint ~'~protosg#)
                    (println ((get-in ~'~protosg# 
                                      [0 2]) 777))
                    (reify ~@reification#))))))))
