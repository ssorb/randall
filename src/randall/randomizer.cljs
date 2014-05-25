(ns randall.randomizer)

(def flat "♭")

(defn as-flat [s]
  (str flat s))

(defn flatten-note [note]
  (str note flat))

(def chromatic [(as-flat 2) "2" (as-flat 3) "3" "4" (as-flat 5) "5" (as-flat 6) "6" (as-flat 7) "7"])

(def chromatic-map [{(as-flat 2) 1}
                    {"2" 2}
                    {(as-flat 3) 3}
                    {"3" 4}
                    {"4" 5}
                    {(as-flat 5) 6}
                    {"5" 7}
                    {(as-flat 6) 8}
                    {"6" 9}
                    {(as-flat 7) 10}
                    {"7" 11}])

(def notes [:C (flatten-note "D") :D (flatten-note "E") :E :F (flatten-note "G") :G (flatten-note "A") :A (flatten-note "B") :B])

(def c-scale [:C :D :E :F :G :A :B])

(def natural-modes [:Ionian :Dorian :Phrygian :Lydian :Mixolydian :Aoelian :Locrian])

(def strings [:E :A :D :G :B :e])

(def string-pairs [[:E :A]
                   [:A :D]
                   [:D :G]
                   [:G :B]
                   [:B :e]])

(def chords [:maj7 :7 :9 (str 7 flat 9) :13 :11 :#11 :sus4 :sus2 (str 7 flat 5) :alt])

(def chord-quality [:maj :min :dim :aug])

(def fingerings
  [1234
   1243
   1324
   1342
   1423
   1432

   2134
   2143
   2314
   2341
   2413
   2431

   3124
   3142
   3214
   3241
   3412
   3421

   4123
   4132
   4213
   4231
   4312
   4321])

(defn rand-n-of [n coll]
  (loop [result [] remaining coll]
    (if (= (count result) n)
      result
      (let [remaining (if (empty? remaining) coll remaining)
            n (rand-nth (shuffle remaining))
            remaining (remove #{n} remaining)]
        (recur (conj result n) remaining)))))

(defn rand-zone []
  (let [start (inc (rand-int 12))]
    (vec (range start (+ 4 start)))))

(defn rand-key []
  (nth notes (rand-int (count notes))))

(defn rand-formula [size]
  (->> chromatic-map
       (rand-n-of size)
       (concat [{:1 0}])
       (apply merge)
       (sort-by val <)
       (mapv first)))

(defn rand-tempo []
  (rand-nth (range 50 (inc 180))))

(defn rand-n-tones []
  (inc (rand-int 12)))

(defn improv
  ([] (improv (rand-n-tones)))
  ([size]
   {:key     (rand-key)
    :formula (rand-formula size)
    :size    size
    :zone    (rand-zone)
    :tempo   (rand-tempo)}))

(defn vamp []
  {:string (rand-nth strings)
   :key    (rand-key)
   :mode   (rand-nth natural-modes)
   :tempo  (rand-tempo)})

(defn vamp-2-strings []
  {:strings (rand-nth string-pairs)
   :key     (rand-key)
   :mode    (rand-nth natural-modes)
   :tempo   (rand-tempo)})

(defn one-string-formula []
  (let [size (rand-n-tones)]
    {:string  (rand-nth strings)
     :formula (rand-formula (rand-n-tones))
     :size    size
     :key     (rand-key)
     :tempo   (rand-tempo)}))

(defn two-string-formula []
  (let [size (rand-n-tones)]
    {:strings (rand-nth string-pairs)
     :formula (rand-formula size)
     :size    size
     :key     (rand-key)
     :tempo   (rand-tempo)}))

(defn rand-fingering []
  (rand-n-of 4 [1 2 3 4]))

(defn rand-fingerings []
  {:fingerings (repeatedly 6 rand-fingering)})

(defn rand-triads
  ([] (rand-triads (max 2 (rand-int 8))))
  ([n]
   (mapv vector
         (rand-n-of n notes)
         (rand-n-of n chord-quality))))

(defn rand-twelve-keys []
  (rand-n-of 12 notes))