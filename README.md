# dynamic-reify

A Clojure macro to specify reification at runtime.

## Usage



    (dynamic-reify (foo     ;; a protocol with methods f1 and f2
                    ((f1 [this]) (fn [t] 1))
                    ((f2 [this that]) (fn [t x] )

                    bar
                    ((f3 [this]) my-function)) ;; a closure

                    ;; if any of its methods evaluate to nil, that protocol
                    ;; is ommited from the reificaton
                    baz
                    ((f4 [this]) evals-to-nil)
                    ((f5 [this]) (if some-condition
                                   conditionall-added-function))))

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
