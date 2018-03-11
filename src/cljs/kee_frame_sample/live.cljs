(ns kee-frame-sample.live
  (:require [re-interval.core :refer [register-interval-handlers]]
            [re-frame.core :refer [reg-event-fx reg-event-db reg-sub debug]]
            [ajax.core :as ajax]
            [kee-frame.core :refer [reg-controller reg-chain]]
            [kee-frame-sample.format :as format]
            [kee-frame-sample.util :as util]
            [clojure.string :as str]))

(register-interval-handlers :live nil 10000)

(reg-controller :live-polling
                {:params (fn [{:keys [handler]}]
                           (when (= handler :live) true))
                 :start  [:live/start]
                 :stop   [:live/stop]})

(reg-controller :live-startup
                {:params (constantly true)
                 :start  [:live/load-matches]})

(reg-event-fx :live/tick
              (fn [_ _] {:dispatch [:live/load-matches true]}))

(def league-id (comp last #(str/split % #"/") :href :competition :_links))

(reg-chain :live/load-matches
           (fn [_ _] {:http-xhrio (util/http-get "http://api.football-data.org/v1/fixtures"
                                                 {:params {:timeFrame :n1}})})
           (fn [{:keys [db]} [_ _ {:keys [fixtures]}]]
             {:db (assoc db :live-matches (->> fixtures
                                               (map #(update % :date format/format-time))
                                               (group-by league-id)))}))