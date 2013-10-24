(ns tesselax.core
  (:require [tesselax.connect :as connect]
            [domina :as dom]))

(defrecord Rect [x y width height color div])

(defn rrange
  [min max]
  (+ min (rand-int (- max min))))

(defn rprop
  [min max prop]
  (rrange (get min prop) (get max prop)))

(defn random-color
  []
  [(rand-int 255) (rand-int 255) (rand-int 255)])

(defn random-rect
  ([min max] (random-rect min max 0))
  ([min max id]
     (let [rrect (partial rprop min max)]
       (Rect. (rrect :x) (rrect :y) (rrect :width) (rrect :height) (random-color) nil))))

(defn rect-pile
  [number width height]
  (let [min (Rect. 0 0 10 10)
        max (Rect. 500 500 width height)]
    (map (partial random-rect min max) (range number))))

(defn update-rect!
  [rect]
  (let [[r g b] (:color rect)
        div (:div rect)
        style (.-style div)]
    (set! (.-top style) (str (:y rect) "px"))
    (set! (.-left style) (str (:x rect) "px"))
    (set! (.-width style) (str (:width rect) "px"))
    (set! (.-height style) (str (:height rect) "px"))
    (set! (.-backgroundColor style) (str "rgb(" r "," g "," b ")"))
    rect))

(defn rect-div!
  [rect]
  (let [div (.createElement js/document "div")]
    (set! (.-className div) "rect")
    (.appendChild (.-body js/document) div)
    (update-rect! (assoc rect :div div))))

(defn scatter-rects
  []
  (let [pile (rect-pile 500 100 100)]
    (mapv rect-div! pile)))

(defn update-pile!
  [pile]
  (mapv update-rect! pile))

(defn horizontal-layout
  [pile]
  (loop [next-x 0
         rest-of-pile pile
         out-pile []]
    (if (empty? rest-of-pile)
      out-pile
      (let [next-rect (first rest-of-pile)]
        (recur 
         (+ next-x (:width next-rect))
         (rest rest-of-pile)
         (conj out-pile (assoc next-rect 
                          :x next-x
                          :y 0)))))))

(connect/connect)

(def pile (scatter-rects))
