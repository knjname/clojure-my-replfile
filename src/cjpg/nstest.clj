;;; -*-coding:utf-8-*-
(ns cjpg.nstest)

;;; nrepl.elのキー
;; C-c C-n nsフォームを評価する
;; C-c M-n REPLの名前空間をカレントバッファの名前空間に変更

;; namespace
(in-ns 'dokodemo.door) ; #<Namespace dokodemo.door> シンボル
(def nobita "Doraemo-n!") ; #'dokodemo.door/nobita

clojure.core/refer ; OK
refer ; java.langはインポートされるが、clojure.coreはインポートされず

(in-ns 'dokodemo.door2) ; #<Namespace dokodemo.door2>
(def nobita "Shizukacha-n!") ; #'dokodemo.door2/nobita
nobita ; "Shizukacha-n!";;; -*-coding:utf-8-*-

(in-ns 'dokodemo.door)
nobita ; "Doraemo-n!"
dokodemo.door2/nobita ; "Shizukacha-n!"

;; create-ns は今のnamespaceを何も変更しない
(def door3 (clojure.core/create-ns 'dokodemo.door3))
nobita ; "Doraemo-n!" 
door3 ; namespace object

;; intern関数を使ってnamespaceオブジェクトに登録できる
(intern door3 'suneo "Only 3 members can play this game! Nobita go home.")
dokodemo.door3/suneo ; "Only ... home." もちろん見られる

(ns-map door3) ; in-nsしたとしてそのまま見られるクラスの一覧. String -> java.lang.String みたいなキーと値の構成.
((ns-map door3) 'String) ; java.lang.String ;  nil なら、oh みられない！ そうじゃないなら、見られる。

;;; まあ、戻りましょう
(ns cjpg.nstest)

;; 外部から見られない変数
(def ^:private privatething 100)
;; そして同じように関数
(defn- privatefunc [n] (+ n 100))

privatething ; みえます
cjpg.nstest/privatething ; みえます
(privatefunc 200) ; みえます
(cjpg.nstest/privatefunc 200) ; みえます

(in-ns 'externalworld)
cjpg.nstest/privatething ; var is not public と怒られます
(cjpg.nstest/privatefunc 200) ; 同様

;;; 戻ります
(ns cjpg.nstest)


