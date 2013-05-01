;;; -*-coding:utf-8-*-

;; 1. Emacsではslimeではなくnreplを使いましょう。
;;
;; 2. ClojureはLeiningen(らいにんげん)経由で起動するのが普通です。
;; Leiningenインスコして、パス通して
;; $> lein new cjpg
;; で
;; cjpg/src/cjpg/core.clj
;; とかができる。
;; core.cljを開いた状態でM-x nrepl-jack-inをするとleiningen経由でクラスパス通ってREPLが起動します。
;; REPL起動時にcjpg/project.cljに依存性書いておけばそいつらもDL＆ロードされます。
;; 依存性増えたらREPL殺してproject.cljに書き足してREPLもっかい起動しましょう。

;; 名前空間宣言
(ns cjpg.core)

;; Clojureはクラスローダがカスタマイズされた奴になってます。
;; クラスロード時にクラスパスから"パッケージ/ファイル.clj"を探してくれます。

;; 古い
(defstruct old-person :first-name :last-name)

;; 新しい
(deftype new-person [first-name last-name])

;; 関数
(defn hello [name]
  (str "Hello, " name))
(hello "Man")                                ; => "Hello, Man"

;; 変(?)数
(def age 18)

;; 例外でるよね
(/ 1 0)
(.printStackTrace *e)                   ; *eは最後の例外を参照

;; 外部ファイルロード
(load-file "./external.clj")
;; 実行パスがベースパスのはずです。
;; lein replしてるならproject.clj置いてる場所がベースパス。

;; 文字列
"str"
"mul
ti
line"
(string? "asdf")                        ; true

;; ベクタ 引数とか変数束縛によく使う
[1 2 3]
;; リスト
'(1 2 3)
;; セット集合
#{}
#{1 3}
#{:a :a}                                ; エラー リテラルでは2重指定はできない

(#{:apple :bacon} :apple) ; true
(#{:apple :bacon} :banana)              ;false

;; 関数の実装として用いてもよい
(defn likeThat? [food] (.contains #{:apple :bacon} food))
(likeThat? :apple) ; true
;; => こうしたほうがCool
(def likeThat? #{:apple :bacon})
(likeThat? :apple) ; true


;; マップ
{:a "Apple" :b "Bacon"}
({:a "Apple" :b "Bacon"} :a)            ; "Apple"
({:a "Apple" :b "Bacon"} :c)            ; nil

;; 逆でもOK
(:a {:a "Apple" :b "Bacon"})            ; "Apple"


;; 正規表現
#"asdf"
(use 'clojure.contrib.str-utils)
(re-split #"\d" "I have 2 apples.")     ; ("I have " " apples.")

;; Boolean
;; false/nil以外は真扱い
true false
(true? true) ;; true
(false? false) ;; true
(false? nil) ;; false
(nil? nil) ;; true
;; Character
\a
;; keyword
:apple
(keyword? :apple)                       ; true
;; nil
nil
;; symbol
java.lang.String
(symbol? java.lang.String)              ; true
;; 数字
0
(zero? 0)
(/ 1 3)

;;; let
(let [a 1]
  (+ a a)) ; 2
(let [a 1 b a]
  (+ a b)) ; 2 (let*)相当
(let [a 10]
  (let [a 1 b a]
    (+ a b))) ; 2 やっぱり(let*)相当

;; if
(if true 1 0)                           ; 1
(if false 1 0)                          ; 0

;;; ループ

;; loop - 末尾再帰のための構文
;; recurに渡した引数はloopのnとsumにそれぞれ渡される
;; もちろんそれぞれの初期値はn=10とsum=0。
(loop [n 10 sum 0]
  (if (zero? n)
    sum
    (recur (- n 1) (+ sum n))))         ; 55
;; 最後のrecurは末尾再帰でなくてはいけない。
;; 要するにrecurの返り値を使ってあれやこれやすると怒られる。
;; 末尾再帰しなきゃいけないので、結果値はループ変数として引き回す必要がある
;; めんどい。

;; 特にloopで初期束縛を行わくてもdefnでもよい
(defn sigma [n sum]
  (if (zero? n)
    sum
    (recur (- n 1) (+ sum n))))
(sigma 100 0)                           ; 5050
(sigma 20 20)                           ; 230
;; こっちのがいいね
(defn sigma
  ([n]
     (sigma n 0))
  ([n sum]
     (if (zero? n)
       sum
       (recur (- n 1) (+ sum n)))))
(sigma 10)                              ; 55


;; 分配 (destructuring)
(let [[a b c] [1 2 3]]
  (+ a b c)) ; 6
(let [{aho :a} {:a 3}]
  (+ aho aho))                          ; 6
(let [{[baka manuke] :a} {:a [30 10]}]
  (* baka manuke))                      ; 300
(let [{alpha :a gamma :r} #{:a :b}]
  (print alpha gamma))                                ; :a nil



;; 関数
(#(* 2 %) 10)                           ; 20
((fn [n] (+ n n)) 15)                ; 30

;; レキシカルクロージャ
(defn lexical-example [num]
  [#(+ % num) #(- % num)])

(let [[add-fn sub-fn] (lexical-example 50)]
  (print (add-fn 100) "and" (sub-fn 30))) ; 150 and -20

;; quasi quote
;; Common Lisp だと `(1 2 ,(+ 1 2))
`(1 2 ~(+ 1 2))                         ; (1 2 3)
;; Common Lisp だと `(1 2 ,@(range 3 10))
`(1 2 ~@(range 3 10))                    ; (1 2 3 4 5 6 7 8 9)

;; conjoin - 結合関数
(conj #{:a} :b)                         ; #{:a :b}

;; リファレンス
(def counter (ref 0))
;; 更新
(alter counter inc)                     ; 例外(No transaction running)発生
(dosync (alter counter inc))            ; 成功
@counter                                ; 参照を外す
(deref counter)                         ; 参照を外す @と同様

(require 'clojure.contrib.str-utils)    ; クラスロード
;; ただしこのようにしてロードしてもFQNで参照しなければならない
(refer 'clojure.contrib.str-utils)      ; こうすると関数などを直接参照できるようになる

(use 'clojure.contrib.str-utils)        ; requireとreferを一緒にやる

;; 自作ライブラリよんでみよーっと
;; ※cjpg/src/jisaku/lib.cljにおいてある(JavaでいえばWEB-INF/classesやbinにあたるクラスパス)
(use 'jisaku.lib)                       ; 一回だけロードされる。中でトップレベルに副作用記述とかあってもちゃんと実行される。
(jisaku-func)                           ; jisaku/lib.cljの中で定義されているjisaku-funcを呼び出す
(use '[jisaku.lib :only (jisaku-func)])

(use :reload 'jisaku.lib)               ; リロードできる
(use :reload-all 'jisaku.lib)           ; 参照先もリロードする場合


(import 'java.io.InputStream)
(import '(java.io File IOException))

;; range関数 3~300, 3ごとの数字列
(range 3 300 3)                         ; => (3 6 ... 300)
;; take関数 リストの要素を先頭から取得
(take 5 (range 2 10))                   ; => (2 3 4 5 6)

;; doc関数 関数についているドキュメントをprint
(doc str)
(find-doc "str")                        ; apropos相当 正規表現マッチする関数のドキュメントをprint

;; concat関数
(concat [1 2] [3 4])                    ; => (1 2 3 4)

;; class関数
(class "asdf")                          ; => java.lang.String

;; オブジェクトのメソッド呼び出し
(.toLowerCase "UPPERCASE")              ; => uppercase

;; str関数 文字列とか文字とか結合
(str "ap" \p "le")                      ; => apple

;; apply関数 関数の適用
(apply str ["ap" \p "le"])              ; => apple

;; interleave関数 要素を交叉させる
(interleave (range 1 10) (range 11 20)) ; => (1 11 2 12 ... 10 20)

;; filter関数
(filter #{\a \b \c} "popcorn candy")    ; => (\c \c \a)

;;; var
(def baz "Gooooooogle")
;; 直接見る
(var baz) ; #'cjpg.core/baz
#'baz ;  #'cjpg.core/baz

;; 名前空間チェンジ
(in-ns 'asdf)
(def ghjk 3) ; #'asdf/ghjk

(in-ns 'clojure)

;; staticフィールドアクセス
java.io.File/separator ; "\\"

;; = は equals
(= "asdf" (str "as" "df")) ; true

;; identity check
(identical? "asdf" (str "as" "df")) ; false

;; meta data参照
(meta #'str) ; str関数のメタデータ参照

(defn
  #^{:tag Integer} str-length ; 関数の返す型
  [#^{:tag String} str] ; 関数の引数の型
  (.length str))

;; 上と同じ
(defn
  #^Integer str-length
  [#^String str]
  (.length str))

(str-length "apple") ; 5
(str-length [1 2 3]) ; ClassCastException

(meta #'str-length) ; {...}

;; metaデータの付け方
(let [bacon (with-meta [1 2] {:my-meta :data-is-here})]
  (meta bacon)) ; {:my-meta :data-is-here}

(def #^{:meta-data :is-on-variable} bacon [1 2])
(meta bacon) ; nil
(meta #'bacon) ; {:meta-data :is-on-variable ...}

;; クラスのnewを呼び出す
(new String "new String")
(String. "new String")
(java.util.Random.)
(new java.util.Random)

;; メソッド呼び出してみよう
(let [r (java.util.Random.)]
  (.nextInt r)
  (.nextInt r 100))

;; staticフィールド
(. java.io.File separator)

;; 連鎖的に参照
(.. "apple" getClass toString) ; class java.lang.String
(.. "apple" (concat " and") (concat " bacon") length) ; 15

;; 何回も参照
(let [m (java.util.HashMap.)]
  (doto m
    (.put "key1" "val1") ; ただここだと他のオブジェクトのメソッド呼び出しできない
    (.put "key2" "val2"))
  m) ; {key1=val1, key2=val2}

;; 配列作成
(make-array String 10) ;; new String[10]
(.getClass (int-array 10)) ;; [I (array of int), プリミティブ型の配列を作成するためのもの

;; なんでもシーケンス化
(seq #{"a" "b" "c"}) ; ("a" "b" "c")

;; 特定文字列を含むドキュメントをすべて表示
(find-doc "tr")

(to-array ["a" "b" "c"]) ; Object[] な配列
(into-array String ["a" "b" "c"]) ; String[] な配列

;; メソッド名を呼び出す関数を作る
((memfn toUpperCase) "asdf") ; "ASDF"
;; cut使えばこんな感じ
(#(.toUpperCase %) "asdf") ; "ASDF"

;; String.formatと同じ
(format "%03d" 3) ; "003"

;; instanceof的な
(instance? String "string") ; true
(instance? Runnable #(+ % 1)) ; true
(instance? Callable #(+ % 1)) ; true

(.getClass "asdf") ; java.lang.String
(class "asdf") ; java.lang.String

;; get/is やらのJavaBeanメソッドからマップを作る
(bean "String") ; { :empty false :class java.lang.String :bytes ~~ }

;; 繰り返し
(dotimes [i 10]
  (print i))

;; 標準出力に経過時刻を出力
(time (map #(* % %) (range 10 10000)))

(first [1 2 3 4]) ; 1
(rest [1 2 3 4]) ; (2 3 4)
(cons 0 '(1 2)) ; (0 1 2)

(seq [1 2 3]) ; (1 2 3)
(next [1 2 3]) ; (2 3)


(sorted-set :zoo :apple :ant) ; #{:ant :apple :zoo}
(sorted-map :zoo 2 :apple 4 :ant 4) ; {:ant 4 :apple 4 :zoo 2}
(hash-set :zoo :apple :ant) ; #{:apple :ant :zoo}
(hash-map :zoo 2 :apple 4 :ant 4) ; {:apple 4 :ant 4 :zoo 2}


(into '(1 2 3) '(:a :b :c)) ; (:c :b :a 1 2 3)

(range 10 20 2) ; (10 12 14 .. 20)
(repeat 100 :apple) ; (:apple :apple ... :apple)

;; take抜きでやると無限に入るので注意
(take 10 (iterate #(* 2 %) 1)) ; (1 2 4 8 .. 512)
(take 10 (cycle '(2 4 6))) ; (2 4 6 2 4 6 .. 2)

(interpose "<=>" "asdf") ; (\a "<=>" \s "<=>" \d "<=>" \f)
(str-join "<=>" ["hoge" "fuga"]) ; clojure-contrib に存在。joinです。

(take-while #(< % 100) (iterate inc 1)) ; (1 2 3 4 .. 99)

((complement #(< % 100)) 20) ; false

(split-at 10 (range 0 12)) ; [(0 1 2 3 .. 9) (10 11)]
(split-with #(< % 10) (range 0 12)) ; [(0 1 2 3 .. 9) (10 11)]

(every? #(#{:iPhone :Android} (:have %))
        [{:name "Sato" :have :iPhone}
         {:name "Kato" :have :Android}]) ; true
;; <=> not-every?

(some #(#{:iPhone :Android} (:have %))
      [{:name "Sato" :have :WindowsPhone}
       {:name "Kato" :have :Tizen}]) ; nil
;; <=> not-any?

(reduce + (range 1 10)) ; 45
(reduce #(format "%s<%s" %1 %2) "0" (range 1 10)) ; 0<1<2..<9

(sort #(< (:age %1) (:age %2)) [{:age 30} {:age 10} {:age 20}]) ; ({:age 10} {:age 20} {:age 30})
(sort-by :age [{:age 30} {:age 10} {:age 20}]) ; ({:age 10} {:age 20} {:age 30})
(sort-by #(:age %) [{:age 30} {:age 10} {:age 20}]) ; ({:age 10} {:age 20} {:age 30})
(sort-by :age > [{:age 30} {:age 10} {:age 20}]) ; ({:age 30} {:age 20} {:age 10})

(for [i (range 0 10)]
  (* i i)) ; (0 1 4 9 .. 81)
(for [i (range 0 10) :when (odd? i)]
  (* i i)) ; (0 1 9 25 49 81)
(for [i (iterate inc 0) :while (< i 10)]
  i) ; (0 .. 9)
(for [i (range 10) j (range 10)]
  [i j]) ; ([0 0] [0 1] .. [9 9])

(use '[clojure.contrib.lazy-seqs])

(def ordinals-and-primes (map vector (iterate inc 1) primes))

(reduce * (take 11 primes))

;; xが評価されるまでprintlnは実行されない
(def x (for [i (range 1 3)]
         (do (println i)
             i)))

(doall x) ; 全評価
(dorun x) ; 全評価、ただし結果は捨てる

(re-seq #"\d{3}" "090a234r9bosfijg332") ; ("090" "234" "332")
(drop 2 (range 3 10)) ; (5 .. 9)

(map #(.getName %) (seq (.listFiles (java.io.File. "."))))

(use '[clojure.contrib.duck-streams])

(with-open [rdr (reader "project.clj")]
  (count (line-seq rdr)))

(with-open [rdr (reader "project.clj")]
  (doall (line-seq rdr)))

(use '[clojure.xml])
(:content (parse (java.io.File. "hoge.xml")))

(peek '(3 4 5)) ; 3
(pop '(3 4 5)) ; (4 5)

(get [1 2 3] 2) ; 3 

(keys {:a 1 :b 2}) ; (:a :b)
(vals {:a 1 :b 2}) ; (1 2)

([4 5 6] 2) ; 6

(assoc [0 3 5 7] 1 :three) ; [0 :three 5 7]

(subvec [1 2 3 4 5] 2 4) ; [3 4]

(select-keys {:a 1 :b 2 :c 3} [:a :c]) ; {:c 3 :a 1}
(merge {:a 10} {:b 20}) ; {:a 10 :b 20}
(merge-with + {:a 10} {:a 20 :b 10}) ; {:a 30 :b 10}

(letfn [(print-hello [name]
          (println name ", hello!"))]
  (print-hello "Mc.Donald"))

(defn infinite-num
  ([] (infinite-num 0))
  ([n] (lazy-seq
        (cons n (infinite-num (+ 1 n))))))

(take 100 (infinite-num)) ; 0 .. 99

(let [lazy-by-hand  (lazy-seq
                     (cons (do (print "1st eval") 1)
                           (lazy-seq (cons (do (print "2nd eval") 2) nil))))]
  (doall lazy-by-hand))

;;; arrow macro
(-> "asdf"
    .toUpperCase ; ASDF
    (.charAt 0) ; \A
    Character. ; \A
    .getClass) ; java.lang.Character

;; namespace
(in-ns 'dokodemo.door) ; #<Namespace dokodemo.door> シンボル
(def nobita "Doraemo-n!") ; #'dokodemo.door/nobita

clojure.core/refer ; OK
refer ; java.langはインポートされるが、clojure.coreはインポートされず

(in-ns 'dokodemo.door2) ; #<Namespace dokodemo.door2>
(def nobita "Shizukacha-n!") ; #'dokodemo.door2/nobita
nobita ; "Shizukacha-n!";;; -*-coding:utf-8-*-

;; 1. Emacsではslimeではなくnreplを使いましょう。
;;
;; 2. ClojureはLeiningen(らいにんげん)経由で起動するのが普通です。
;; Leiningenインスコして、パス通して
;; $> lein new cjpg
;; で
;; cjpg/src/cjpg/core.clj
;; とかができる。
;; core.cljを開いた状態でM-x nrepl-jack-inをするとleiningen経由でクラスパス通ってREPLが起動します。
;; REPL起動時にcjpg/project.cljに依存性書いておけばそいつらもDL＆ロードされます。
;; 依存性増えたらREPL殺してproject.cljに書き足してREPLもっかい起動しましょう。

;; 名前空間宣言
(ns cjpg.core)

;; Clojureはクラスローダがカスタマイズされた奴になってます。
;; クラスロード時にクラスパスから"パッケージ/ファイル.clj"を探してくれます。

;; 古い
(defstruct old-person :first-name :last-name)

;; 新しい
(deftype new-person [first-name last-name])

;; 関数
(defn hello [name]
  (str "Hello, " name))
(hello "Man")                                ; => "Hello, Man"

;; 変(?)数
(def age 18)

;; 例外でるよね
(/ 1 0)
(.printStackTrace *e)                   ; *eは最後の例外を参照

;; 外部ファイルロード
(load-file "./external.clj")
;; 実行パスがベースパスのはずです。
;; lein replしてるならproject.clj置いてる場所がベースパス。

;; 文字列
"str"
"mul
ti
line"
(string? "asdf")                        ; true

;; ベクタ 引数とか変数束縛によく使う
[1 2 3]
;; リスト
'(1 2 3)
;; セット集合
#{}
#{1 3}
#{:a :a}                                ; エラー リテラルでは2重指定はできない

(#{:apple :bacon} :apple) ; true
(#{:apple :bacon} :banana)              ;false

;; 関数の実装として用いてもよい
(defn likeThat? [food] (.contains #{:apple :bacon} food))
(likeThat? :apple) ; true
;; => こうしたほうがCool
(def likeThat? #{:apple :bacon})
(likeThat? :apple) ; true


;; マップ
{:a "Apple" :b "Bacon"}
({:a "Apple" :b "Bacon"} :a)            ; "Apple"
({:a "Apple" :b "Bacon"} :c)            ; nil

;; 逆でもOK
(:a {:a "Apple" :b "Bacon"})            ; "Apple"


;; 正規表現
#"asdf"
(use 'clojure.contrib.str-utils)
(re-split #"\d" "I have 2 apples.")     ; ("I have " " apples.")

;; Boolean
;; false/nil以外は真扱い
true false
(true? true) ;; true
(false? false) ;; true
(false? nil) ;; false
(nil? nil) ;; true
;; Character
\a
;; keyword
:apple
(keyword? :apple)                       ; true
;; nil
nil
;; symbol
java.lang.String
(symbol? java.lang.String)              ; true
;; 数字
0
(zero? 0)
(/ 1 3)

;;; let
(let [a 1]
  (+ a a)) ; 2
(let [a 1 b a]
  (+ a b)) ; 2 (let*)相当
(let [a 10]
  (let [a 1 b a]
    (+ a b))) ; 2 やっぱり(let*)相当

;; if
(if true 1 0)                           ; 1
(if false 1 0)                          ; 0

;;; ループ

;; loop - 末尾再帰のための構文
;; recurに渡した引数はloopのnとsumにそれぞれ渡される
;; もちろんそれぞれの初期値はn=10とsum=0。
(loop [n 10 sum 0]
  (if (zero? n)
    sum
    (recur (- n 1) (+ sum n))))         ; 55
;; 最後のrecurは末尾再帰でなくてはいけない。
;; 要するにrecurの返り値を使ってあれやこれやすると怒られる。
;; 末尾再帰しなきゃいけないので、結果値はループ変数として引き回す必要がある
;; めんどい。

;; 特にloopで初期束縛を行わくてもdefnでもよい
(defn sigma [n sum]
  (if (zero? n)
    sum
    (recur (- n 1) (+ sum n))))
(sigma 100 0)                           ; 5050
(sigma 20 20)                           ; 230
;; こっちのがいいね
(defn sigma
  ([n]
     (sigma n 0))
  ([n sum]
     (if (zero? n)
       sum
       (recur (- n 1) (+ sum n)))))
(sigma 10)                              ; 55


;; 分配 (destructuring)
(let [[a b c] [1 2 3]]
  (+ a b c)) ; 6
(let [{aho :a} {:a 3}]
  (+ aho aho))                          ; 6
(let [{[baka manuke] :a} {:a [30 10]}]
  (* baka manuke))                      ; 300
(let [{alpha :a gamma :r} #{:a :b}]
  (print alpha gamma))                                ; :a nil



;; 関数
(#(* 2 %) 10)                           ; 20
((fn [n] (+ n n)) 15)                ; 30

;; レキシカルクロージャ
(defn lexical-example [num]
  [#(+ % num) #(- % num)])

(let [[add-fn sub-fn] (lexical-example 50)]
  (print (add-fn 100) "and" (sub-fn 30))) ; 150 and -20

;; quasi quote
;; Common Lisp だと `(1 2 ,(+ 1 2))
`(1 2 ~(+ 1 2))                         ; (1 2 3)
;; Common Lisp だと `(1 2 ,@(range 3 10))
`(1 2 ~@(range 3 10))                    ; (1 2 3 4 5 6 7 8 9)

;; conjoin - 結合関数
(conj #{:a} :b)                         ; #{:a :b}

;; リファレンス
(def counter (ref 0))
;; 更新
(alter counter inc)                     ; 例外(No transaction running)発生
(dosync (alter counter inc))            ; 成功
@counter                                ; 参照を外す
(deref counter)                         ; 参照を外す @と同様

(require 'clojure.contrib.str-utils)    ; クラスロード
;; ただしこのようにしてロードしてもFQNで参照しなければならない
(refer 'clojure.contrib.str-utils)      ; こうすると関数などを直接参照できるようになる

(use 'clojure.contrib.str-utils)        ; requireとreferを一緒にやる

;; 自作ライブラリよんでみよーっと
;; ※cjpg/src/jisaku/lib.cljにおいてある(JavaでいえばWEB-INF/classesやbinにあたるクラスパス)
(use 'jisaku.lib)                       ; 一回だけロードされる。中でトップレベルに副作用記述とかあってもちゃんと実行される。
(jisaku-func)                           ; jisaku/lib.cljの中で定義されているjisaku-funcを呼び出す
(use '[jisaku.lib :only (jisaku-func)])

(use :reload 'jisaku.lib)               ; リロードできる
(use :reload-all 'jisaku.lib)           ; 参照先もリロードする場合


(import 'java.io.InputStream)
(import '(java.io File IOException))

;; range関数 3~300, 3ごとの数字列
(range 3 300 3)                         ; => (3 6 ... 300)
;; take関数 リストの要素を先頭から取得
(take 5 (range 2 10))                   ; => (2 3 4 5 6)

;; doc関数 関数についているドキュメントをprint
(doc str)
(find-doc "str")                        ; apropos相当 正規表現マッチする関数のドキュメントをprint

;; concat関数
(concat [1 2] [3 4])                    ; => (1 2 3 4)

;; class関数
(class "asdf")                          ; => java.lang.String

;; オブジェクトのメソッド呼び出し
(.toLowerCase "UPPERCASE")              ; => uppercase

;; str関数 文字列とか文字とか結合
(str "ap" \p "le")                      ; => apple

;; apply関数 関数の適用
(apply str ["ap" \p "le"])              ; => apple

;; interleave関数 要素を交叉させる
(interleave (range 1 10) (range 11 20)) ; => (1 11 2 12 ... 10 20)

;; filter関数
(filter #{\a \b \c} "popcorn candy")    ; => (\c \c \a)

;;; var
(def baz "Gooooooogle")
;; 直接見る
(var baz) ; #'cjpg.core/baz
#'baz ;  #'cjpg.core/baz

;; 名前空間チェンジ
(in-ns 'asdf)
(def ghjk 3) ; #'asdf/ghjk

(in-ns 'clojure)

;; staticフィールドアクセス
java.io.File/separator ; "\\"

;; = は equals
(= "asdf" (str "as" "df")) ; true

;; identity check
(identical? "asdf" (str "as" "df")) ; false

;; meta data参照
(meta #'str) ; str関数のメタデータ参照

(defn
  #^{:tag Integer} str-length ; 関数の返す型
  [#^{:tag String} str] ; 関数の引数の型
  (.length str))

;; 上と同じ
(defn
  #^Integer str-length
  [#^String str]
  (.length str))

(str-length "apple") ; 5
(str-length [1 2 3]) ; ClassCastException

(meta #'str-length) ; {...}

;; metaデータの付け方
(let [bacon (with-meta [1 2] {:my-meta :data-is-here})]
  (meta bacon)) ; {:my-meta :data-is-here}

(def #^{:meta-data :is-on-variable} bacon [1 2])
(meta bacon) ; nil
(meta #'bacon) ; {:meta-data :is-on-variable ...}

;; クラスのnewを呼び出す
(new String "new String")
(String. "new String")
(java.util.Random.)
(new java.util.Random)

;; メソッド呼び出してみよう
(let [r (java.util.Random.)]
  (.nextInt r)
  (.nextInt r 100))

;; staticフィールド
(. java.io.File separator)

;; 連鎖的に参照
(.. "apple" getClass toString) ; class java.lang.String
(.. "apple" (concat " and") (concat " bacon") length) ; 15

;; 何回も参照
(let [m (java.util.HashMap.)]
  (doto m
    (.put "key1" "val1") ; ただここだと他のオブジェクトのメソッド呼び出しできない
    (.put "key2" "val2"))
  m) ; {key1=val1, key2=val2}

;; 配列作成
(make-array String 10) ;; new String[10]
(.getClass (int-array 10)) ;; [I (array of int), プリミティブ型の配列を作成するためのもの

;; なんでもシーケンス化
(seq #{"a" "b" "c"}) ; ("a" "b" "c")

;; 特定文字列を含むドキュメントをすべて表示
(find-doc "tr")

(to-array ["a" "b" "c"]) ; Object[] な配列
(into-array String ["a" "b" "c"]) ; String[] な配列

;; メソッド名を呼び出す関数を作る
((memfn toUpperCase) "asdf") ; "ASDF"
;; cut使えばこんな感じ
(#(.toUpperCase %) "asdf") ; "ASDF"

;; String.formatと同じ
(format "%03d" 3) ; "003"

;; instanceof的な
(instance? String "string") ; true
(instance? Runnable #(+ % 1)) ; true
(instance? Callable #(+ % 1)) ; true

(.getClass "asdf") ; java.lang.String
(class "asdf") ; java.lang.String

;; get/is やらのJavaBeanメソッドからマップを作る
(bean "String") ; { :empty false :class java.lang.String :bytes ~~ }

;; 繰り返し
(dotimes [i 10]
  (print i))

;; 標準出力に経過時刻を出力
(time (map #(* % %) (range 10 10000)))

(first [1 2 3 4]) ; 1
(rest [1 2 3 4]) ; (2 3 4)
(cons 0 '(1 2)) ; (0 1 2)

(seq [1 2 3]) ; (1 2 3)
(next [1 2 3]) ; (2 3)


(sorted-set :zoo :apple :ant) ; #{:ant :apple :zoo}
(sorted-map :zoo 2 :apple 4 :ant 4) ; {:ant 4 :apple 4 :zoo 2}
(hash-set :zoo :apple :ant) ; #{:apple :ant :zoo}
(hash-map :zoo 2 :apple 4 :ant 4) ; {:apple 4 :ant 4 :zoo 2}


(into '(1 2 3) '(:a :b :c)) ; (:c :b :a 1 2 3)

(range 10 20 2) ; (10 12 14 .. 20)
(repeat 100 :apple) ; (:apple :apple ... :apple)

;; take抜きでやると無限に入るので注意
(take 10 (iterate #(* 2 %) 1)) ; (1 2 4 8 .. 512)
(take 10 (cycle '(2 4 6))) ; (2 4 6 2 4 6 .. 2)

(interpose "<=>" "asdf") ; (\a "<=>" \s "<=>" \d "<=>" \f)
(str-join "<=>" ["hoge" "fuga"]) ; clojure-contrib に存在。joinです。

(take-while #(< % 100) (iterate inc 1)) ; (1 2 3 4 .. 99)

((complement #(< % 100)) 20) ; false

(split-at 10 (range 0 12)) ; [(0 1 2 3 .. 9) (10 11)]
(split-with #(< % 10) (range 0 12)) ; [(0 1 2 3 .. 9) (10 11)]

(every? #(#{:iPhone :Android} (:have %))
        [{:name "Sato" :have :iPhone}
         {:name "Kato" :have :Android}]) ; true
;; <=> not-every?

(some #(#{:iPhone :Android} (:have %))
      [{:name "Sato" :have :WindowsPhone}
       {:name "Kato" :have :Tizen}]) ; nil
;; <=> not-any?

(reduce + (range 1 10)) ; 45
(reduce #(format "%s<%s" %1 %2) "0" (range 1 10)) ; 0<1<2..<9

(sort #(< (:age %1) (:age %2)) [{:age 30} {:age 10} {:age 20}]) ; ({:age 10} {:age 20} {:age 30})
(sort-by :age [{:age 30} {:age 10} {:age 20}]) ; ({:age 10} {:age 20} {:age 30})
(sort-by #(:age %) [{:age 30} {:age 10} {:age 20}]) ; ({:age 10} {:age 20} {:age 30})
(sort-by :age > [{:age 30} {:age 10} {:age 20}]) ; ({:age 30} {:age 20} {:age 10})

(for [i (range 0 10)]
  (* i i)) ; (0 1 4 9 .. 81)
(for [i (range 0 10) :when (odd? i)]
  (* i i)) ; (0 1 9 25 49 81)
(for [i (iterate inc 0) :while (< i 10)]
  i) ; (0 .. 9)
(for [i (range 10) j (range 10)]
  [i j]) ; ([0 0] [0 1] .. [9 9])

(use '[clojure.contrib.lazy-seqs])

(def ordinals-and-primes (map vector (iterate inc 1) primes))

(reduce * (take 11 primes))

;; xが評価されるまでprintlnは実行されない
(def x (for [i (range 1 3)]
         (do (println i)
             i)))

(doall x) ; 全評価
(dorun x) ; 全評価、ただし結果は捨てる

(re-seq #"\d{3}" "090a234r9bosfijg332") ; ("090" "234" "332")
(drop 2 (range 3 10)) ; (5 .. 9)

(map #(.getName %) (seq (.listFiles (java.io.File. "."))))

(use '[clojure.contrib.duck-streams])

(with-open [rdr (reader "project.clj")]
  (count (line-seq rdr)))

(with-open [rdr (reader "project.clj")]
  (doall (line-seq rdr)))

(use '[clojure.xml])
(:content (parse (java.io.File. "hoge.xml")))

(peek '(3 4 5)) ; 3
(pop '(3 4 5)) ; (4 5)

(get [1 2 3] 2) ; 3 

(keys {:a 1 :b 2}) ; (:a :b)
(vals {:a 1 :b 2}) ; (1 2)

([4 5 6] 2) ; 6

(assoc [0 3 5 7] 1 :three) ; [0 :three 5 7]

(subvec [1 2 3 4 5] 2 4) ; [3 4]

(select-keys {:a 1 :b 2 :c 3} [:a :c]) ; {:c 3 :a 1}
(merge {:a 10} {:b 20}) ; {:a 10 :b 20}
(merge-with + {:a 10} {:a 20 :b 10}) ; {:a 30 :b 10}

(letfn [(print-hello [name]
          (println name ", hello!"))]
  (print-hello "Mc.Donald"))

(defn infinite-num
  ([] (infinite-num 0))
  ([n] (lazy-seq
        (cons n (infinite-num (+ 1 n))))))

(take 100 (infinite-num)) ; 0 .. 99

(let [lazy-by-hand  (lazy-seq
                     (cons (do (print "1st eval") 1)
                           (lazy-seq (cons (do (print "2nd eval") 2) nil))))]
  (doall lazy-by-hand))

;;; arrow macro
(-> "asdf"
    .toUpperCase ; ASDF
    (.charAt 0) ; \A
    Character. ; \A
    .getClass) ; java.lang.Character

(-> a b c d e) ; (e (d (c (b a)))) と同じ

;; namespace
;; (名前空間 イライラする！)
(clojure.core/all-ns) ; すべてのnamespaceを見ることができる

(in-ns 'dokodemo.door) ; #<Namespace dokodemo.door> シンボル
(def nobita "Doraemo-n!") ; #'dokodemo.door/nobita

clojure.core/refer ; OK
refer ; java.langはインポートされるが、clojure.coreはインポートされず
*ns* ; 今の名前空間が見られます

(clojure.core/refer 'clojure.core) ; core見られないのは不便なのでこれで対処
refer ; 見られるようになります。

(in-ns 'dokodemo.door2) ; #<Namespace dokodemo.door2>
(def nobita "Shizukacha-n!") ; #'dokodemo.door2/nobita
nobita ; "Shizukacha-n!"

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
(ns cjpg.core)

;; 外部から見られない変数
(def ^:private privatething 100)
;; そして同じように関数
(defn- privatefunc [n] (+ n 100))

privatething ; みえます
cjpg.core/privatething ; みえます
(privatefunc 200) ; みえます
(cjpg.core/privatefunc 200) ; みえます

(in-ns 'externalworld)
cjpg.core/privatething ; var is not public と怒られます
(cjpg.core/privatefunc 200) ; 同様

;;; 戻ります
(ns cjpg.core)

;; Futureを使う計算
(import [java.util.concurrent Executors] )
(let [ext (Executors/newCachedThreadPool)
      ^Callable delayed #(+ 3 4)]
  (.get (.submit ext delayed)))

;; interface定義
(definterface HumbleFolk
  (^String sayHello [^String name]))

;; 実装してみよう
(deftype Japanese [familyname] HumbleFolk
         (^String sayHello [_ ^String name] (str "Konnichiwa, " familyname " " name))) ; _ はthisが入る。Pythonっぽい

(deftype American [nickname] HumbleFolk
         (^String sayHello [_ ^String name] (str "Hello, " name " the " nickname)))

(-> (Japanese. "Suzuki")
    (.sayHello "Ichiro")) ; "Konnichiwa, Suzuki Ichiro"

(-> (American. "Poo")
    (.sayHello "Winnie")) ; "Hello, Winnie the Poo"

(deftype Italiano [familyname] HumbleFolk
         (^String sayHello [this ^String name] (str "Ciao, " name " " (.familyname this)))) ; this をちゃんと使ってみよう

(-> (Italiano. "bros")
    (.sayHello "Mario")) ; Ciao, Mario bros

;; プロトコル

(defprotocol Sound
  (sounds [this]))

(deftype 日光 [] Sound
         (sounds [_] "ぽかぽか"))

(defn- strRepeat [n repeated]
  (apply str (repeat n repeated)))

(deftype 蚊 [numberOfMosquitoes] Sound
         (sounds [_] (strRepeat numberOfMosquitoes "ブーン")))

(-> (日光.)
    sounds) ; ぽかぽか
(-> (蚊. 3)
    sounds) ; ブーンブーンブーン

(extends? Sound 蚊) ; true
(extends? Sound 日光) ; true
(extends? Sound String) ; false

;; defmacro

(declare enclose) ; ちょいと後で紹介

;; 指定回数分式をくるむマクロ
(defmacro enclose-n [n core & encloser]
  (enclose n core encloser))

;; マクロの下請け関数。マクロだけで処理すると面倒なので関数に渡す。
;; こういうのは(letfn ) でマクロ内に置いてもいいですけど、defn-でやったほうが楽っちゃ楽。
(defn- enclose [n body encloser]
  (if (= n 0)
    body
    (recur (- n 1) `(~@encloser ~body) encloser)))


;; 10回くるんでる
;; (* 2 (* 2 (* 2 (* 2 (* 2 (* 2 (* 2 (* 2 (* 2 (* 2 5))))))))))
(macroexpand '(enclose-n 10 5 * 2))

;; 同じようにn回マクロ展開するマクロを書いてみる
(defmacro macroexpand-n [n form]
  (enclose n form '(macroexpand-1)))

;; 3回マクロ展開している
;; (clojure.core/-> (clojure.core/-> (clojure.core/-> (clojure.core/-> a b) c) d) e f g h i)
(macroexpand-n 3 '(-> a b c d e f g h i))

;; 下記は同じ結果になる
(macroexpand-1 '(-> a b c d e f g h i))
(macroexpand-n 1 '(-> a b c d e f g h i))

;; macroexpand-n の引数nはコンパイル時に自然数(java.lang.Number)でなければならない。
;; よってこういう呼び出しはできない
(macroexpand-n (+ 1 2) '(-> a b)) ; clojure.lang.PersistentList cannot be cast to java.lang.Number

;; 上記のようなことをやりたいのであれば、それは関数を使うべき場面だったということ。
;; そういうことで、なんでもかんでも関数をマクロにできるわけではないし、その逆も然り。
;; しかし、マクロを使っていれば一度コンパイルした後なら展開後のコードがそのまま実行される。
;; 場合によってはパフォーマンスで有利になる。

;; マクロを使って新しいマクロを作ってみよう
(defmacro macroexpand-2 [form]
  (macroexpand-n 2 form))

;; こう定義したのと全く等価であるが、はるかに見やすい。
(defmacro ugly-macroexpand-2 [form]
  (macroexpand-1 (macroexpand-1 form)))

;; もっと定義してみよう
(defmacro macroexpand-3 [form]
  (macroexpand-n 3 form))

;; ... これを10までやろうと思うんだけど、めんどい
;; じゃあマクロをいっぱい定義するマクロを作りましょう

;; これを実行したら%%%が2～10までの数字に置換されればいいな
(form-n-times 2 10
              (defmacro macroexpand-%%% [form]
                (macroexpand-n %%% form)))

;; じゃ、道具から
(defn rewrite-index-symbol [sym rewriteWith]
  (-> sym
      str
      (.replaceAll "%%%" (str rewriteWith))
      symbol
      ))

(defn replace-form-index [idx form]
  (clojure.walk/postwalk
   #(if (symbol? %)
      (rewrite-index-symbol % idx)
      %)
   form))

(defmacro form-n-times [start end form]
  `(do ~@(for [i (range start (inc end))]
           (replace-form-index i form))))

;; 実行
;; これはうまくいかない。
;; まず、内部の(macroexpand-n %%% form)が実行されるが、%%%が数字じゃないからダメだ。
(form-n-times
 2 10
 (defmacro macroexpand-%%% [form]
   (macroexpand-n %%% form)))

;; こっちはうまくいくんだけどね
(macroexpand
 '(form-n-times
   2 10
   (defmacro macroexpand-%%% [form]
     (macroexpand-n %%% form))))

