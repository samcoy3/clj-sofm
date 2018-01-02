(ns coursework.core
  (:require [clojure.data.json :as json])
  (:require [coursework.display :as display])
  (:require [coursework.util :as util])
  (:use (incanter core charts stats))
  (:gen-class))

; This is the project core. The main function target is -main.

(def ms-per-update 0) ; the number of miliseconds that the sofm should delay by between each iteration (useful for making the animation easier to see)

(def sofm-matrix (atom [])) ; the threadsafe matrix to be used by the update function

; GENERATION
(defn random-point
  "Generates a random point in (0,1]^2"
  []
  (apply vector (repeatedly 2 rand))) 

(defn generate-random-matrix
  "For a given <size>, generates a <size>^2-length vector containing random points in (0,1]^2"
  ; it's worth noting that the chosen representation of this 2d structure is 1d
  ; util/neighbours finds the neighbours of any given index in the 1d array, so a 2d representation is unnecessary and harder to cleanly interate over
  [size]
  (mapv (fn[x](random-point)) (range (* size size))))

; CONVERGENCE
; These are the functions used to measure convergence
(def convergence-measures {"centre-point" (fn [mat]
                                  (let [centre (get mat (/ (- (count mat) 1) 2))]
                                    (util/distance centre [0.5 0.5])))
               })

; HEURISTICS
; These are the functions used to modiffy the rate of convergence to the selected points over time.
(def lambda-schedules {"given" (fn [iter]
                          (let [l (/ 1 (+ 1 (Math/log10 iter)))]
                            [l (/ l 2)]))
              })

; UTIL
(defn adjust-point
  "Adjusts <point> by moving it <lambda> closer to the drawn point <sample>"
  [point lambda sample]
  (let [delta (mapv #(* lambda %) (map - sample point))]
    (mapv + delta point)))

; MAIN LOOP
(defn update-matrix
  "Updates the <matrix> of size <size> of random points <iterations> number of times. Uses <lambdas> as the annealing schedule, and uses <convergence> to measure convergence of the sofm. Returns the list of convergence values at each iteration."
  [size iterations lambdas convergence]
  (loop [iter iterations convergence-values ()]
    (let [point (random-point)
          mat @sofm-matrix
          nearest (util/nearest-index mat point)
          [lambda gamma] (lambdas (- iterations iter -1))]
      (display/update-sofm mat point) ; updates the information that the display module has
      (Thread/sleep ms-per-update) ; sleeps temporarily, useful for the animation
      (if (= iter 0)
        convergence-values ; if this is the final iteration return the list of convergence values
        (recur (dec iter) ; otherwise iterate, decrementing the iterations, and passing a modified matrix into the loop
               (do ; first update the matrix, then work out the convergence of the new matrix
                 (let [replacements (conj
                                      (hash-map (nth mat nearest) (adjust-point (nth mat nearest) lambda point))
                                      (zipmap (map #(nth mat %) (util/neighbours size nearest))
                                             (map #(adjust-point (nth mat %) gamma point) (util/neighbours size nearest))))]
                 (swap! sofm-matrix (partial replace replacements)))
                 (cons (convergence @sofm-matrix) convergence-values)
                 ))))))

(defn -main
  ([]
    (-main :generic-error))
  ([single-arg]
    (case single-arg
      ("help" "-help" "--help") (println "This program takes three mandatory arguments: size of the SOFM, number of SOFMs to train, and the number of iterations to be performed on each SOFM.\nFor example, '3 10 1000' as an argument list would train 10 3x3 SOFMs for 1000 iterations each.\nFor more information please consult the README.")
      :generic-error (println "The program was run incorrectly. Please consult the README or run the program with the 'help' flag"))
    (System/exit 0))
  ([size quant iters]
    (-main size quant iters "given" "centre-point"))
  ([size quant iters lambda-schedule convergence-measure]
    (display/init) ; initialise the display module
    (reset! sofm-matrix (generate-random-matrix (read-string size))) ; resets the matrix atom to a random matrix
    (loop [sofm-number (read-string quant) converge-values ()]
      (let [next-converge-values (update-matrix (read-string size) (read-string iters) (get lambda-schedules lambda-schedule) (get convergence-measures convergence-measure))
        new-converge-values (if (empty? converge-values) next-converge-values (map + next-converge-values converge-values))]
      (if (= sofm-number 0)
        (view (trace-plot (reverse new-converge-values)))
        (recur (dec sofm-number) new-converge-values))))))
