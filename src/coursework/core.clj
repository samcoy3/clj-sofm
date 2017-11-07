(ns coursework.core
  (:gen-class))

(defn random-point
  "Generates a random point in (0,1]^2"
  []
  (apply vector (repeatedly 2 rand))) 

(defn generate-random-matrix
  "For a given <size>, generates a <size>^2-length vector containing random points in (0,1]^2"
  [size]
  (mapv (fn[x](random-point)) (range (* size size))))

(defn lambdas 
  "For a given <iter>, returns a 2-length vector containing lambda and gamma"
  [iter]
  (let [l (/ 1 (+ 1 (Math/log10 iter)))]
    [l (/ l 2)]))

(defn distance
  "Returns the distance between <point1> and <point2>"
  [point1 point2]
  (Math/sqrt
    (+ 
      (Math/pow (- (nth point1 0) (nth point2 0)) 2)
      (Math/pow (- (nth point1 1) (nth point2 1)) 2))))

(defn adjust-point
  "Adjusts <point> by moving it <lambda> closer to the drawn point <sample>"
  [point lambda sample]
  (let [delta (mapv #(* lambda %) (map - sample point))]
    (mapv + delta point)))

(defn nearest-index
  "Returns the index of the nearest point in <matrix> to <point>"
  [matrix point]
  (first (apply min-key second (map-indexed vector (mapv #(distance point %) matrix)))))

(defn neighbours
  "Returns a lazy sequence of neighbouring indices given a matrix <size> and <index>"
  [size index]
  (let [index2-d (vector (quot index size) (rem index size))]
    (map #(+ (* (first %) size) (second %))
         (filter #(and (< (first %) size)
                  (< (second %) size)
                  (< -1 (first %))
                  (< -1 (second %)))
            (mapv #(mapv + index2-d %) [[0 1] [0 -1] [1 0] [-1 0]])))))

(defn update-matrix
  "Updates the <matrix> of size <size> of random points <iterations> number of times"
  [matrix size iterations]
  (loop [iter iterations mat matrix]
    (let [point (random-point)
          nearest (nearest-index mat point)
          [lambda gamma] (lambdas (- iterations iter -1))]
      (if (= iter 0)
        mat
        (recur (dec iter)
               (replace
                   (conj
                    (hash-map (nth mat nearest) (adjust-point (nth mat nearest) lambda point))
                    (zipmap (map #(nth mat %) (neighbours size nearest))
                           (map #(adjust-point (nth mat %) gamma point) (neighbours size nearest))))
                   mat))))))

(defn -main
  [& args]
  (let [m (generate-random-matrix 3)]
    (println (update-matrix m 3 1000000))))
