(ns tesselax.space)

(defrecord Rect [x y width height])

(defn area
  [{:keys [width height]}]
  (* width height))

(defn inside?
  "first argument is a rect
   second argument is a point represented by a two element vector"
  [{:keys [x y width height]} [a b]]
  (and
   (< x a (+ x width))
   (< y b (+ y height))))

(defn non-overlap
  "Determines if two rectangles do not overlap."
  [a b]
  (letfn [(above [{y :y h :height} {limit :y}]
            (<= (+ y h) limit))
          (left [{x :x w :width} {limit :x}]
            (<= (+ x w) limit))]
    (or (above a b)
        (above b a)
        (left a b)
        (left b a))))

(def overlap?
  "determines if two rectangles overlap"
  (comp not non-overlap))

(defn fits-inside?
  [rect space]
  (and 
   (< (:width rect) (:width space))
   (or (nil? (:height space))
       (< (:height rect) (:height space)))))

(defn add-rect
  "grid - all rects already placed
   rect - rect we want to fit into the grid
   spaces - a list of all spaces remaining"
  [grid rect spaces]
  (if-let [space (first (filter (partial fits-inside? rect) spaces))]
    (let [fit (assoc rect
                :x (:x space)
                :y (:y space))
          grid (conj grid fit)
          reduced-space (assoc space 
                          :x (+ (:x space) (:width rect))
                          :width (- (:width space) (:width rect)))
          below-space (assoc space
                        :y (+ (:y space) (:height rect))
                        :height (if (nil? (:height space)) 
                                  nil
                                  (- (:height space) (:height rect))))]
      [grid [reduced-space below-space]])
    [grid spaces]))
