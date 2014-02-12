(ns tesselax.layout  
  (:require [tesselax.space :as space]))

(def infinity (/ 1.0 0.0))

(defprotocol Sized
  (size [this])
  (position [this])
  (resize! [this size])
  (reposition! [this position]))

(defprotocol LayoutContainer
  (setup! [this opts])
  (fixed [this])
  (flowing [this]))

(defrecord Rect [x y width height node])

(defn rect 
  [sized]
  (map->Rect (merge {:node sized} (size sized) (position sized))))

(defn horizontal-limit-layout
  ([pile container] (horizontal-limit-layout [] pile container))
  ([fixed pile container] 
     (let [x-limit (:width (size container))
           [grid spaces] 
           (reduce
            (fn [[grid spaces] rect]
              (space/add-rect grid rect spaces))
            [[] [(Rect. 0 0 x-limit infinity nil)]] fixed)]
       (loop [pile pile
              out-pile grid
              spaces spaces]
         (if (empty? pile)
           [out-pile spaces]
           (let [next-rect (first pile)
                 fit-rect (space/next-open-position next-rect spaces)
                 [grid spaces] (space/add-rect out-pile fit-rect spaces)]
             (recur (rest pile) grid spaces)))))))

(defn layout-child!
  [rect]
  {:pre [(satisfies? Sized (:node rect))]}
  (reposition! (:node rect) {:x (:x rect) :y (:y rect)}))

(defn layout!
  [container opts]
  (let [fixed (map rect (fixed container))
        pile (map rect (flowing container))
        layout (or (:layout opts) horizontal-limit-layout)
        [updated-pile spaces] (layout fixed pile container)]
    (doseq [rect updated-pile] (layout-child! rect))
    [pile spaces]))

(defn init!
  "container must implement Sized and LayoutContainer,
   whose children must implement Sized as well."
  ([container] (init! container {}))
  ([container opts]
    {:pre [(satisfies? LayoutContainer container)
           (satisfies? Sized container)]}
    (setup! container opts)
    (layout! container opts)))
