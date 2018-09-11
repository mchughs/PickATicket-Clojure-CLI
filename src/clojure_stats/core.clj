(ns clojure-stats.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io  :as io])
  (:gen-class))

(defn -main
  [args]
  (with-open
    ;; reads in the csv file provided through args on the command line
    [reader (io/reader args)]
    ;; the CSV is converted to a list of vectors of the plays
    ;; however the built-in csv capabilities of clojure have trouble with
    ;; the format of this particular csv file.
    ;; read-csv may expect sections to be seperated by a new line.
    (let [corruptedList (csv/read-csv reader)]
      ;; removes anything that isn't a [title, date, genre] vector from the list
      (let [shows (filter (fn [item] (= (count item) 3)) corruptedList)]
          ;; initialize all of the counts
          (def dramaCount   0)
          (def comedyCount  0)
          (def musicalCount 0)
          (def monthCounts (vec (repeat 12 0)))
          
          ;; doseq loops through the shows and allows side effects like incrementing the counts
          (doseq [show shows]
            ;; pulls off the date and genre from the show and increments the relevant counts
            (let [date (nth show 1) genre (nth show 2)]
              (cond
                (= genre "DRAMA")   (def dramaCount   (inc dramaCount))
                (= genre "COMEDY")  (def comedyCount  (inc comedyCount))
                (= genre "MUSICAL") (def musicalCount (inc musicalCount))
                )
              ;; extracts the month from the date using a regex
              (let [month (Integer. (nth (re-seq #"(?<=-)[0-9]+(?=-)" date) 0))]
                (def monthCounts (assoc
                  monthCounts
                  (- month 1) ;; replace value at this index
                  (inc (get monthCounts (- month 1))) ;; with its current value + 1
                )))
            )
          )

          ;; packages the information into a map/hashtable
          (def genreCounts {:DRAMA dramaCount :COMEDY comedyCount :MUSICAL musicalCount})
          (println "Genre counts: " genreCounts)
          ;; find the index of the largest value in the map and returns the associated key
          (def mostPopular (key (apply max-key val genreCounts)))
          (println "The most popular genre is " mostPopular)

          (println "Number of shows by month Jan-Dec: " monthCounts)
      )
    )
  )
)
