(ns tesselax.shared.space)

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
   (< (:height rect) (:height space))))

(defn rect-right
  [rect]
  (+ (:x rect) (:width rect)))

(defn rect-bottom
  [rect]
  (+ (:y rect) (:height rect)))

(defn fully-contains?
  [outer inner]
  (and 
   (<= (:x outer) (:x inner) (rect-right inner) (rect-right outer))
   (<= (:y outer) (:y inner) (rect-bottom inner) (rect-bottom outer))))

(defn space-ordering
  [a b]
  (if (= (:y a) (:y b))
    (< (:x a) (:x b))
    (< (:y a) (:y b))))

(defn overlaps-right?
  [rect space]
  (< (:x space) (rect-right rect) (rect-right space)))

(defn overlaps-bottom?
  [rect space]
  (< (:y space) (rect-bottom rect) (rect-bottom space)))

(defn overlaps-left?
  [rect space]
  (< (:x space) (:x rect) (rect-right space)))

(defn overlaps-top?
  [rect space]
  (< (:y space) (:y rect) (rect-bottom space)))

(defn squish-right
  [rect space]
  (let [right (rect-right rect)
        overlap-width (- right (:x space))]
    (assoc space
      :x right
      :width (- (:width space) overlap-width))))

(defn squish-bottom
  [rect space]
  (let [bottom (rect-bottom rect)
        overlap-height (- bottom (:y space))]
    (assoc space
      :y bottom
      :height (if (:height space) (- (:height space) overlap-height)))))

(defn squish-left
  [rect space]
  (assoc space
    :width (- (:x rect) (:x space))))

(defn squish-top
  [rect space]
  (assoc space
    :height (- (:y rect) (:y space))))

(defn null-space?
  [space]
  (or (nil? space) (= 0 (:height space)) (= 0 (:width space))))

(defn partition-space
  [rect space]
  (let [right-space (if (overlaps-right? rect space) (squish-right rect space))
        bottom-space (if (overlaps-bottom? rect space) (squish-bottom rect space))
        left-space (if (overlaps-left? rect space) (squish-left rect space))
        top-space (if (overlaps-top? rect space) (squish-top rect space))
        new-spaces (list right-space bottom-space left-space top-space)]
    (remove null-space? new-spaces)))

(defn print-rect 
  [rect]
  (str (:x rect) "," (:y rect) "," (:width rect) "," (:height rect)))

(defn add-rect
  "grid - all rects already placed
   rect - rect we want to fit into the grid
   spaces - a list of all spaces remaining"
  [grid rect spaces]
  (if-let [space (first (filter (partial fits-inside? rect) spaces))]
    (let [fit (merge rect (select-keys space [:x :y]))
          grid (conj grid fit)

          {overlapping true non-overlapping false}
          (group-by
           (fn [rect]
             (overlap? fit rect)) 
           spaces)

          partitioned-spaces (mapcat (partial partition-space fit) overlapping)
          collapsed-spaces (reduce
                            (fn [keepers part]
                              (if (first
                                   (filter
                                    #(fully-contains? % part)
                                    keepers))
                                keepers (conj keepers part)))
                            (list) (concat non-overlapping partitioned-spaces))

          spaces-in-order (sort space-ordering collapsed-spaces)]
      [grid spaces-in-order])
    [grid spaces]))
