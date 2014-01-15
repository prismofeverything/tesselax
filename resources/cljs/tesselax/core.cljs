(ns tesselax.core
  (:require [tesselax.connect :as connect]
            [tesselax.example :as example]))

  ;; (:require [cljs.core.async :refer [<! >! timeout chan close! put! alts! sliding-buffer]]
  ;;           [tesselax.connect :as connect]
  ;;           [tesselax.space :as space]
  ;;           [tesselax.example :as example]
  ;;           [domina :as dom]
  ;;           [domina.css :as css]
  ;;           [domina.events :as events])
  ;; (:require-macros [cljs.core.async.macros :refer [go alt!]]))

;; (defrecord Rect [x y width height color div])

;; (def grid (atom (list)))
;; (def spaces (atom (list)))
;; (def resize-channel (chan (sliding-buffer 1)))

;; (defn rrange
;;   ([min max]
;;      (+ min (rand-int (- max min))))
;;   ([min max curve]
;;      (let [min-output (double min)
;;            output-range (double (- max min))
;;            curved-range (.log js/Math (* output-range curve))
;;            position (rand curved-range)
;;            curved-position (.exp js/Math position)
;;            result (+ min-output position)]
;;        (.log js/console "rrange " min max curve " = " result)
;;        result)))

;; (defn rprop
;;   [min max prop]
;;   (rrange (get min prop) (get max prop)))

;; (defn random-color
;;   []
;;   [(rand-int 200) (rand-int 200) (rand-int 200)])

;; (defn random-rect
;;   ([min max] (random-rect min max 0))
;;   ([min max id]
;;      (let [rrect (partial rprop min max)]
;;        (Rect. (rrect :x) (rrect :y) (rrect :width) (rrect :height) (random-color) nil))))

;; (defn rect-pile
;;   [number width height]
;;   (let [min (Rect. 0 0 100 100)
;;         max (Rect. 500 500 width height)]
;;     (map (partial random-rect min max) (range number))))

;; (defn update-rect!
;;   [rect]
;;   (let [[r g b] (:color rect)
;;         div (:div rect)
;;         style (.-style div)]
;;     (set! (.-top style) (str (:y rect) "px"))
;;     (set! (.-left style) (str (:x rect) "px"))
;;     (set! (.-width style) (str (:width rect) "px"))
;;     (set! (.-height style) (str (:height rect) "px"))
;;     (set! (.-backgroundColor style) (str "rgb(" r "," g "," b ")"))
;;     ;; (set! (.-backgroundImage style) (str "url(http://placekitten.com/" (:width rect) "/" (:height rect) ")"))
;;     ;; (set! (.-backgroundImage style) (str "url(http://flickholdr.com/" (:width rect) "/" (:height rect) ")"))
;;     rect))

;; (defn update-space!
;;   [space]
;;   (let [[r g b] (:color space)
;;         div (:div space)
;;         style (.-style div)]
;;     (set! (.-className div) "space")
;;     (set! (.-top style) (str (dec (:y space)) "px"))
;;     (set! (.-left style) (str (dec (:x space)) "px"))
;;     (set! (.-width style) (str (:width space) "px"))
;;     (set! (.-height style) (str (:height space) "px"))
;;     (set! (.-borderStyle style) "solid")
;;     (set! (.-borderWidth style) "1px")
;;     (set! (.-borderColor style) (str "rgb(" r "," g "," b ")"))
;;     (set! (.-backgroundColor style) (str "rgba(" r "," g "," b ",0.1)"))
;;     space))

;; (defn show-rect!
;;   [rect]
;;   (let [div (:div rect)
;;         style (.-style div)]
;;     (set! (.-opacity style) 1)))

;; (defn hide-rect!
;;   [rect]
;;   (let [div (:div rect)
;;         style (.-style div)]
;;     (set! (.-opacity style) 0)))

;; (defn rect-div!
;;   ([rect] (rect-div! rect update-rect!))
;;   ([rect update]
;;      (let [div (.createElement js/document "div")]
;;        (set! (.-className div) "rect")
;;        (.appendChild (.-body js/document) div)
;;        (update (assoc rect :div div)))))

;; (defn scatter-rects
;;   [n]
;;   (let [pile (rect-pile n 400 400)]
;;     (mapv rect-div! pile)))

;; (defn update-pile!
;;   [pile]
;;   (mapv update-rect! pile))

;; (defn animate-pile!
;;   [pile ms update]
;;   (go
;;    (doseq [rect pile]
;;      (show-rect! rect)
;;      (update rect)
;;      (<! (timeout ms)))))

;; (defn horizontal-layout
;;   [pile]
;;   (loop [next-x 0
;;          rest-of-pile pile
;;          out-pile []]
;;     (if (empty? rest-of-pile)
;;       out-pile
;;       (let [next-rect (first rest-of-pile)]
;;         (recur
;;          (+ next-x (:width next-rect))
;;          (rest rest-of-pile)
;;          (conj out-pile (assoc next-rect
;;                           :x next-x
;;                           :y 0)))))))

;; (defn area
;;   [{:keys [width height]}]
;;   (* width height))

;; (defn inside?
;;   "first argument is a rect
;;    second argument is a point represented by a two element vector"
;;   [{:keys [x y width height]} [a b]]
;;   (and
;;    (< x a (+ x width))
;;    (< y b (+ y height))))

;; (defn all-points
;;   [{:keys [x y width height]}]
;;   (let [x' (+ x width)
;;         y' (+ y height)]
;;     [[x y]
;;      [x' y]
;;      [x' y']
;;      [x y']]))

;; (defn cruciform?
;;  [a b]
;;  (letfn [(cross? [{x :x y :y w :width h :height}
;;                   {x' :x y' :y w' :width h' :height}]
;;            (and (<= x x' (+ x' w') (+ x w))
;;                 (<= y' y (+ y h) (+ y' h'))))]
;;    (or (cross? a b)
;;        (cross? b a))))

;; (defn old-overlap?
;;   [a b]
;;   (or
;;    (some (partial inside? b) (all-points a))
;;    (some (partial inside? a) (all-points b))
;;    (cruciform? a b)))

;; (defn non-overlap
;;   "Determines if two rectangles do not overlap."
;;   [a b]
;;   (letfn [(above [{y :y h :height} {limit :y}]
;;             (<= (+ y h) limit))
;;           (left [{x :x w :width} {limit :x}]
;;             (<= (+ x w) limit))]
;;     (or (above a b)
;;         (above b a)
;;         (left a b)
;;         (left b a))))

;; (def overlap?
;;   "determines if two rectangles overlap"
;;   (comp not non-overlap))

;; (defn random-nonoverlap-layout
;;   [pile max-x max-y]
;;   (loop [placed []
;;          pile pile
;;          max-x max-x
;;          max-y max-y]
;;     (if (empty? pile)
;;       placed
;;       (let [rect (first pile)
;;             x (rand-int max-x)
;;             y (rand-int max-y)
;;             rect (assoc rect :x x :y y)]
;;         (if (some (partial overlap? rect) placed)
;;           (do
;;             (.log js/console "Expanding... !" max-x)
;;             (recur placed pile (+ max-x 10) (+ max-y 10)))
;;           (recur (conj placed rect) (rest pile) max-x max-y))))))

;; (defn less-random-nonoverlap-layout
;;   [pile max-x max-y]
;;   (loop [placed []
;;          rejects []
;;          pile pile
;;          max-x max-x
;;          max-y max-y]
;;     (if (empty? pile)
;;       (if (empty? rejects)
;;         placed
;;         (do
;;           (.log js/console
;;                 "Expanding... ! rejects: " (count rejects)
;;                 " --- max bounds:" max-x)
;;           (recur placed [] rejects (+ max-x 50) (+ max-y 50))))
;;       (let [rect (first pile)
;;             x (rand-int max-x)
;;             y (rand-int max-y)
;;             rect (assoc rect :x x :y y)]
;;         (if (some (partial overlap? rect) placed)
;;           (recur placed (conj rejects rect) (rest pile) max-x max-y)
;;           (recur (conj placed rect) rejects (rest pile) max-x max-y))))))

;; (defn horizontal-limit-layout
;;   [pile x-limit]
;;   (loop [pile pile
;;          out-pile []
;;          spaces [(Rect. 0 0 x-limit js/Infinity [0 0 0])]]
;;     (if (empty? pile)
;;       [out-pile spaces]
;;       (let [next-rect (first pile)
;;             [grid spaces] (space/add-rect out-pile next-rect spaces)]
;;         (.log js/console (count spaces))
;;         (recur
;;          (rest pile)
;;          grid
;;          spaces)))))

;; (defn remove-spaces!
;;   []
;;   (dom/destroy! (dom/by-class "space")))

;; (defn animate-grid!
;;   [pile width]
;;   (let [[pile spaces] (horizontal-limit-layout pile width)]
;;     (remove-spaces!)
;;     (animate-pile! pile 0 update-rect!)
;;     ;; (animate-pile! (sort-by (fn [x] (rand)) pile) 20 update-rect!)
;;     (animate-pile! (mapv #(rect-div! % update-space!) (map #(update-in % [:height] (partial min 500)) spaces)) 0 update-space!)
;;     pile))

;; (defn resize-grid
;;   [event]
;;   (.log js/console event)
;;   (swap! grid #(animate-grid! % (.-innerWidth js/window))))

;; (defn debouncer
;;   [inflow interval]
;;   (let [debounce (chan)]
;;     (go
;;      (loop [channels [inflow]
;;             event nil]
;;        (let [[value channel] (alts! channels)]
;;          (if (= channel inflow)
;;            (recur [inflow (timeout interval)] value)
;;            (do
;;              (put! debounce event)
;;              (recur [inflow] nil))))))
;;     debounce))

;; (defn resize!
;;   [event]
;;   (put! resize-channel event))

;; (defn reflow-on-resize
;;   []
;;   (let [debounced-resize (debouncer resize-channel 50)]
;;     (go
;;      (while true
;;        (resize-grid (<! debounced-resize))))))

;; (defn init!
;;   []
;;   (let [pile (scatter-rects 50)]
;;     (reflow-on-resize)
;;     (reset! grid (animate-grid! pile (.-innerWidth js/window)))))

;; (def on-load
;;   (do
;;     (set! (.-onload js/window) init!)
;;     (set! (.-onresize js/window) resize!)))

(connect/connect)

;; (def pile (reverse (sort-by area (scatter-rects 50))))
;; (let [[grid spaces] (horizontal-limit-layout pile (.-innerWidth js/window))]
;;   (animate-pile! (sort-by (fn [x] (rand)) grid) 20 update-rect!)
;;   (animate-pile! (mapv #(rect-div! % update-space!) (map #(update-in % [:height] (partial min 500)) spaces)) 20 update-space!))
