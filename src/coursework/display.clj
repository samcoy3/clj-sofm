(ns coursework.display
  (:require [coursework.util :as util])
  (:use seesaw.core
        seesaw.color
        seesaw.graphics))

(def data [[0 0] 0])

(defn paint-sofm
  "Paints a representation of the current state of the sofm"
  [c g]
  (let [w (.getWidth c)
        h (.getHeight c)
        new-point (second data)
        mat (first data)
        size (int (Math/sqrt (+ 1 (.length mat))))]
    (draw g
      (circle (* w (first new-point))
              (* h (second new-point))
              5)
      (style :background (color "#0223FC")))
    (doseq [[index [x y]] (map vector (range) mat)]
      (draw g
        (circle (* w x)
                (* h y)
                5)
        (style :background (color "#FC021F")))
        (println (util/neighbours size index))
        (doseq [point (util/neighbours size index)]
          (let [[x2 y2] (get mat point)]
            (draw g
              (line (* w x)
                    (* h y)
                    (* w x2)
                    (* h y2))
              (style :foreground (color "#FC021F") :stroke 2))
            )))
    (push g)
    ))

(def jframe
  (frame :title "Hello"
     :height 200
     :width 200
     :on-close :exit
     :content (canvas :id :canvas
                      :background "#DDDDDD"
                      :paint paint-sofm)))

(defn update-sofm
  "Stores a local copy of the current state of the sofm, so that <paint> can draw from it"
  [mat new-point]
  (.repaint (select jframe [:#canvas]))
  (def data (vector mat new-point)))

(defn make-frame
  []
  (native!)
  (invoke-later
    (-> jframe show!)))

(defn init
  "Initialises the graphics"
  []
  (make-frame))

