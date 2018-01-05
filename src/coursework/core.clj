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
(def convergence-measures {:centre-point (fn [mat]
                                  (let [centre (get mat (/ (- (count mat) 1) 2))]
                                    (util/distance centre [0.5 0.5])))
               })

; HEURISTICS
; These are the functions used to modiffy the rate of convergence to the selected points over time.
(def lambda-schedules {:variable-base (fn [modifier iter]
                          (let [l (/ 1 (+ 1 (/ (Math/log iter) (Math/log modifier))))]
                            [l (/ l 2)]))
              
                       :variable-ratio (fn [modifier iter]
                           (let [l (/ 1 (+ 1 (Math/log10 iter)))]
                             [l (* l modifier)]))
                       })

; UTIL
(defn adjust-point
  "Adjusts <point> by moving it <lambda> closer to the drawn point <sample>"
  [point lambda sample]
  (let [delta (mapv #(* lambda %) (map - sample point))]
    (mapv + delta point)))

; TRAINING
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

(defn run
  "Runs the simulation of the SOFM with the options provided"
  [options]
  (if (:visuals options) (display/init)) ; initialise the real time display if we want to
  (reset! sofm-matrix (generate-random-matrix (:size options))) ; initialise the matrix to a random SOFM of size provided in the options
  (loop [sofm-number (:quant options) converge-values []]
    (let [new-converge-values (conj converge-values (update-matrix (:size options) (:iters options) (partial ((:lambda-type options) lambda-schedules) (:lambda-modifier options)) (:centre-point convergence-measures)))]
      (if (= sofm-number 0)
        (if (:repl-mode options)
          (reverse (apply map + new-converge-values)) ; if repl-mode is on simply return the data
          (view (line-chart (range (:quant options)) (reverse (apply map + new-converge-values))))) ; view the chart
        (recur (dec sofm-number) new-converge-values))))
  )
