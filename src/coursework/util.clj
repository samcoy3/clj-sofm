(ns coursework.util
  )

(defn distance
  "Returns the distance between <point1> and <point2>"
  [point1 point2]
  (Math/sqrt
    (+ 
      (Math/pow (- (nth point1 0) (nth point2 0)) 2)
      (Math/pow (- (nth point1 1) (nth point2 1)) 2))))

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

(defn nearest-index
  "Returns the index of the nearest point in <matrix> to <point>"
  [matrix point]
  (first (apply min-key second (map-indexed vector (mapv #(distance point %) matrix)))))
