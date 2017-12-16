(ns coursework.display
  (:require [coursework.util :as util])
  (:use seesaw.core
        seesaw.color
        seesaw.graphics))

; This file is the display module for the project.
; It displays the sofm every time the matrix is updated with new values.

(def data [[0 0] 0]) ; This is the data variable that is updated whenever the matrix updates. The first vector will represent the matrix, the second will represent the randomly selected point.

(defn paint-sofm
  "Paints a representation of the current state of the sofm, where <c> is the canvas, and <g> is the associated Graphics2d object.
  For more information on paint functions please consult the seesaw documentation."
  [c g]
  (let [w (.getWidth c)
        h (.getHeight c)
        new-point (second data)
        mat (first data)
        size (int (Math/sqrt (+ 1 (.length mat))))]
    (draw g ; draws a circle to represent the randomly selected point
      (circle (* w (first new-point))
              (* h (second new-point))
              5)
      (style :background (color "#0223FC")))
    (doseq [[index [x y]] (map vector (range) mat)] ; iterates over all points in the matrix, zipping the index with the coordinate value
      (draw g ; draws a circle for each point in the matrix
        (circle (* w x)
                (* h y)
                5)
        (style :background (color "#FC021F")))
        (doseq [point (filter #(< index %) (util/neighbours size index))] ; iterates over all neighbours of each point (the filter is to ensure each neighbour-pair is iterated over only once)
          (let [[x2 y2] (get mat point)]
            (draw g ; draws a line between a point and a neighbour
              (line (* w x)
                    (* h y)
                    (* w x2)
                    (* h y2))
              (style :foreground (color "#FC021F") :stroke 2))
            )))
    (push g)
    ))

(def jframe ; defines the JFrame that will be used for the display
  (frame :title "Kohonen SOFM Training"
     :height 400
     :width 400
     :on-close :exit
     :content (canvas :id :canvas
                      :background "#DDDDDD"
                      :paint paint-sofm))) ; this is the paint function defined earlier

(defn update-sofm
  "Stores a local copy of the current state of the sofm, so that <paint> can draw from it"
  [mat new-point]
  (.repaint (select jframe [:#canvas])) ; forces a repaint of the canvas
  (def data (vector mat new-point)))

(defn init
  []
  (native!) ; ensures native window only is used
  (invoke-later
    (-> jframe show!))) ; shows the JFrame, called on init only
