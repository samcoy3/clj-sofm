(ns coursework.core
  (:require [clojure.data.json :as json])
  (:require [coursework.display :as display])
  (:require [coursework.util :as util])
  (:gen-class))

(def netsize 7)
(def ms-per-update 10)

; GENERATION
(defn random-point
  "Generates a random point in (0,1]^2"
  []
  (apply vector (repeatedly 2 rand))) 

(defn generate-random-matrix
  "For a given <size>, generates a <size>^2-length vector containing random points in (0,1]^2"
  [size]
  (mapv (fn[x](random-point)) (range (* size size))))

; HEURISTICS
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
      (display/update-sofm mat point)
      (Thread/sleep ms-per-update)
      (if (= iter 0)
        mat
        (recur (dec iter)
               (replace
                   (conj
                    (hash-map (nth mat nearest) (adjust-point (nth mat nearest) lambda point))
                    (zipmap (map #(nth mat %) (util/neighbours size nearest))
                           (map #(adjust-point (nth mat %) gamma point) (util/neighbours size nearest))))
                   mat))))))

(defn -main
  [& args]
  (display/init)
  (let [m (generate-random-matrix netsize)]
    (println (json/write-str (update-matrix m netsize 1000000)))))
