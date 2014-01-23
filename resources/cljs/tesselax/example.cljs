(ns tesselax.example
  (:require [tesselax.layout :as layout] 
            [tesselax.container :as container]))

(defn rrange
  ([min max]
     (+ min (rand-int (- max min))))
  ([min max curve]
     (let [min-output (double min)
           output-range (double (- max min))
           curved-range (.log js/Math (* output-range curve))
           position (rand curved-range)
           curved-position (.exp js/Math position)
           result (+ min-output position)]
       (.log js/console "rrange " min max curve " = " result)
       result)))

(defn rprop
  [min max prop]
  (rrange (get min prop) (get max prop)))

(defn random-color
  []
  [(rand-int 200) (rand-int 200) (rand-int 200)])

(defn random-rect
  ([min max] (random-rect min max 0))
  ([min max id]
     (let [rrect (partial rprop min max)]
       (layout/Rect. (rrect :x) (rrect :y) (rrect :width) (rrect :height) (random-color)))))

(defn update-rect!
  [rect]
  (let [[r g b] (:node rect)
        div (:div rect)
        style (.-style div)]
    (set! (.-top style) (str (:y rect) "px"))
    (set! (.-left style) (str (:x rect) "px"))
    (set! (.-width style) (str (:width rect) "px"))
    (set! (.-height style) (str (:height rect) "px"))
    (set! (.-backgroundColor style) (str "rgb(" r "," g "," b ")"))
    ;; (set! (.-backgroundImage style) (str "url(http://placekitten.com/" (:width rect) "/" (:height rect) ")"))
    rect))

(defn rect-div!
  ([rect] (rect-div! rect update-rect!))
  ([rect update]
     (let [div (.createElement js/document "div")
           style (.-style div)
           [r g b] (:node rect)]
       (set! (.-className div) "rect")
       (set! (.-width style) (str (:width rect) "px"))
       (set! (.-height style) (str (:height rect) "px"))
       (set! (.-backgroundColor style) (str "rgb(" r "," g "," b ")"))
       (.appendChild (.-body js/document) div)
       (update (assoc rect :div div)))))

(defn rect-pile
  [number width height]
  (let [min (layout/Rect. 0 0 100 100 nil)
        max (layout/Rect. 500 500 width height nil)]
    (map (partial random-rect min max) (range number))))

(defn scatter-rects
  [n]
  (let [pile (rect-pile n 400 400)]
    (mapv rect-div! pile)))

(defn init!
  []
  (let [pile (scatter-rects 50)]
    (container/init-google-layout "body")))

(def on-load
  (set! (.-onload js/window) init!))
