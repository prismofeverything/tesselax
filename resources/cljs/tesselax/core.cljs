(ns tesselax.core
  (:require [cljs.core.async :refer [<! >! timeout chan close!]]
            [tesselax.connect :as connect]
            [domina :as dom])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defrecord Rect [x y width height color div])

(defn rrange
  ([min max]
     (+ min (rand-int (- max min))))
  ([min max curve]
     (let [min-output min
           output-range (- max min)
           curved-range (Math/log (* output-range curve))
           position (rand curved-range)
           curved-position (Math/exp position)]
       (+ min-output position))))

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

(defn show-rect!
  [rect]
  (let [div (:div rect)
        style (.-style div)]
    (set! (.-opacity style) 1)))

(defn hide-rect!
  [rect]
  (let [div (:div rect)
        style (.-style div)]
    (set! (.-opacity style) 0)))

(defn rect-div!
  [rect]
  (let [div (.createElement js/document "div")]
    (set! (.-className div) "rect")
    (.appendChild (.-body js/document) div)
    (update-rect! (assoc rect :div div))))

(defn scatter-rects
  [n]
  (let [pile (rect-pile n 100 100)]
    (mapv rect-div! pile)))

(defn update-pile!
  [pile]
  (mapv update-rect! pile))

(defn animate-pile!
  [pile ms]
  (go
   (doseq [rect pile]
     (show-rect! rect)
     (update-rect! rect)
     (<! (timeout ms)))))

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

(defn inside?
  "first argument is a rect
   second argument is a point represented by a two element vector"
  [{:keys [x y width height]} [a b]]
  (and
   (<= x a (+ x width))
   (<= y b (+ y height))))

(defn all-points
  [{:keys [x y width height]}]
  (let [x' (+ x width)
        y' (+ y height)]
    [[x y]
     [x' y]
     [x' y']
     [x y']]))

(defn cruciform?
  [a b]
  false)

(defn overlap?
  [a b]
  (or
   (some (partial inside? b) (all-points a))
   (some (partial inside? a) (all-points b))
   (cruciform? a b)))

(defn random-nonoverlap-layout
  [pile max-x max-y]
  (loop [placed []
         pile pile
         max-x max-x
         max-y max-y]
    (if (empty? pile)
      placed
      (let [rect (first pile)
            x (rand-int max-x)
            y (rand-int max-y)
            rect (assoc rect :x x :y y)]
        (if (some (partial overlap? rect) placed)
          (do
            (.log js/console "Expanding... !" max-x)
            (recur placed pile (+ max-x 10) (+ max-y 10)))
          (recur (conj placed rect) (rest pile) max-x max-y))))))

(defn less-random-nonoverlap-layout
  [pile max-x max-y]
  (loop [placed []
         rejects []
         pile pile
         max-x max-x
         max-y max-y]
    (if (empty? pile)
      (if (empty? rejects)
        placed
        (do 
          (.log js/console "Expanding... ! rejects: " (count rejects) " --- max bounds:" max-x)
          (recur placed [] rejects (+ max-x 50) (+ max-y 50))))
      (let [rect (first pile)
            x (rand-int max-x)
            y (rand-int max-y)
            rect (assoc rect :x x :y y)]
        (if (some (partial overlap? rect) placed)
          (recur placed (conj rejects rect) (rest pile) max-x max-y)
          (recur (conj placed rect) rejects (rest pile) max-x max-y))))))

(connect/connect)

(def pile (scatter-rects 500))
(animate-pile! (less-random-nonoverlap-layout pile 500 500) 50)
