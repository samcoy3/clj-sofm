(ns coursework.core
  (:require [clojure.data.json :as json])
  (:require [coursework.display :as display])
  (:require [coursework.util :as util])
  (:gen-class))

; This is the project core. The main function target is -main.

(def netsize 7) ; the size of the sofm
(def iters 1000000) ; the number of iterations the sofm should perform
(def ms-per-update 2) ; the number of miliseconds that the sofm should delay by between each iteration (useful for making the animation easier to see)

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

; HEURISTICS
; These are the functions used to modiffy the rate of convergence to the selected points over time.
(defn lambdas 
  "For a given <iter>, returns a 2-length vector containing lambda and gamma"
  [iter]
  (let [l (/ 1 (+ 1 (Math/log10 iter)))]
    [l (/ l 2)]))

; UTIL
(defn adjust-point
  "Adjusts <point> by moving it <lambda> closer to the drawn point <sample>"
  [point lambda sample]
  (let [delta (mapv #(* lambda %) (map - sample point))]
    (mapv + delta point)))

; MAIN LOOP
(defn update-matrix
  "Updates the <matrix> of size <size> of random points <iterations> number of times"
  [matrix size iterations]
  (loop [iter iterations mat matrix]
    (let [point (random-point)
          nearest (util/nearest-index mat point)
          [lambda gamma] (lambdas (- iterations iter -1))]
      (display/update-sofm mat point) ; updates the information that the display module has
      (Thread/sleep ms-per-update) ; sleeps temporarily, useful for the animation
      (if (= iter 0)
        mat ; if this is the final iteration return the matrix itself
        (recur (dec iter) ; otherwise iterate, decrementing the iterations, and passing a modified matrix into the loop
               (replace
                   (conj
                    (hash-map (nth mat nearest) (adjust-point (nth mat nearest) lambda point))
                    (zipmap (map #(nth mat %) (util/neighbours size nearest))
                           (map #(adjust-point (nth mat %) gamma point) (util/neighbours size nearest))))
                   mat))))))

(defn -main
  [& args]
  (display/init) ; initialise the display module
  (let [m (generate-random-matrix netsize)] ; generate an initial matrix
    (println (json/write-str (update-matrix m netsize iters))))) ; run the simulation
