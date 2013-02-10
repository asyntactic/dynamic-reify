(ns dynamic-reify.core)

(defn make-protos [defs]
  (loop [results [] remaining defs]
    (if (empty? remaining)
      results
      (let [proto (first remaining)
            [funs remain] (split-with (complement symbol?)
                                      (rest remaining))]
        ;; omit protos with nil funs
        (recur (if (some (comp nil? second) funs) 
                 results
                 (conj results (vec (concat [proto] 
                                            (mapcat (fn [[fun-args form]]
                                                      [(vec fun-args)
                                                       (eval form)])
                                                    funs)))))
               remain)))))

(defmacro dynamic-reify [defs]
  (let [protosg# (vary-meta (gensym) merge {:dynamic true})]
    `(do
       (def ~protosg#)
       (binding [~protosg# (make-protos '~defs)]
         (let [reification# 
               (apply concat 
                      (map-indexed
                       (fn [idx# [proto# & funs#]] 
                         (concat [proto#]
                                 (map (fn [[[name# args#] func#]]
                                        (list name# args# 
                                              `(apply (get-in ~'~protosg# 
                                                              [~idx# 2])
                                                      ~args#)))
                                      (partition 2 funs#))))
                       ~protosg#))]
           (eval `(let [~'~protosg# ~'~protosg#]
                    (reify ~@reification#))))))))