;;; -*-coding:utf-8-*-
(ns cjpg.macros)

;;; MACRO!!!!!!!!!!!!!!!!!!!

;; マクロをやる前に、nreplのキーを覚えよう。
;; ＜マクロを展開するコマンド＞
;; C-c C-m macroexpand-1を実行
;; C-c M-m macroexpand-allを実行

;; defmacro

;; (doall (for ...)) がめんどい人向け
(defmacro forall [& body]
  (doall `(for @~body)))

;; 式をくるむマクロを書いてみよう
;; たとえば、こんなの。

;; こうかいたら
(macroexpand '(enclose-n 10 5 * 2))
;; ↓のようになってほしい
;; (* 2 (* 2 (* 2 (* 2 (* 2 (* 2 (* 2 (* 2 (* 2 (* 2 5))))))))))

;; 道具の準備
;; 指定回数分式をくるむマクロ
(declare enclose)
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

;; enclosde-n の引数nはコンパイル時に自然数(java.lang.Number)でなければならない。
;; よってこういう呼び出しはできない
(defn multiply [a b]
  (enclose-n b 0 + a))

;; なので、これはOK
(defn multiply-ten [a]
  (enclose-n 10 0 + a))
;; (def multiply-ten (fn* ([a] (+ a (+ a (+ a (+ a (+ a (+ a (+ a (+ a (+ a (+ a 0)))))))))))))

;; 上記のようなことをやりたいのであれば、それは関数を使うべき場面だったということ。
;; そういうことで、なんでもかんでも関数をマクロにできるわけではないし、その逆も然り。
;; しかし、マクロを使っていれば一度コンパイルした後なら展開後のコードがそのまま実行される。
;; 場合によってはパフォーマンスで有利になる。

;; マクロならmultiplyは作れる。
(defmacro multiply [n a]
  `(enclose-n ~n 0 + ~a))

(multiply 10 1000) ; 10000

(defmacro multiply-1 [a]
  (multiply 1 a))

(defmacro multiply-2 [a]
  (multiply 2 a))

(defmacro multiply-3 [a]
  (multiply 3 a))

;; この調子で100まで定義したいが根性が持たない
;; こうかければいいのだが
(n-times 1 100
         (defmacro multiply-%%% [a] (multiply %%% a)))

;; その道具を作ろう

(defn rewrite-index-symbol [sbl num]
  "渡されたシンボルの%%%の部分を指定数字に書き換える。%%%だけならシンボルではなく数字をそのまま返す。"
  (if (= sbl '%%%)
    num
    (-> sbl
        str
        (.replaceAll "%%%" (str num))
        symbol)))

(let [v (rewrite-index-symbol '%%% 100)]
  [v (.getClass v)]) ; [100 java.lang.Long]

(let [v (rewrite-index-symbol 'multiply-%%% 100)]
  [v (.getClass v)]) ; [multiply-100 clojure.lang.Symbol]

(defn replace-form-index [idx form]
  (clojure.walk/postwalk
   #(if (symbol? %)
      (rewrite-index-symbol % idx)
      %)
   form))

;; (defmacro multiply-100 [a] (multiply 100 a))
(replace-form-index
 100
 '(defmacro multiply-%%% [a] (multiply %%% a)))

(defmacro n-times [start end form]
  `(do ~@(map #(replace-form-index % form)
              (range start (inc end)))))

;; ようやく準備はできた
(n-times 1 100
         (defmacro multiply-%%% [a] (multiply %%% a)))

;; これと同じ
(do
  (defmacro multiply-1 [a] (multiply 1 a))
  (defmacro multiply-2 [a] (multiply 2 a))
  (defmacro multiply-3 [a] (multiply 3 a))
  (defmacro multiply-4 [a] (multiply 4 a))
  :
  (defmacro multiply-100 [a] (multiply 100 a)))

(multiply-100 100) ; 10000


;; これはうまくいかない(nのようなSymbolじゃなくNumberくださいといわれる)。
;; マクロの定義ミスでnがコンパイル時に決定している必要があるからだ
;; (逆に言うとコンパイル時決定を要請しているなら、
;; コンパイル後は計算後の数値がそのままソースに配置され、実行時計算は発生しない)
(let [n 100]
  (multiply-100 n))

;; 展開のあるn=100に対しての式はこうなるべきだ
(defmacro multiply-100 [a]
  `(multiply 100 ~a))

(multiply-100 100) ;; 数えきれないぐらい囲まれた(+ 100 (+ 100 ... (+ 100 0)))

;; こうすべきだった
(n-times 1 100
         ;; ~%%% にしないと、たとえば 100 という名前のシンボルになってしまう!
         (defmacro multiply-%%% [a] `(multiply ~%%% ~a)))

;; いける。
(let [n 100]
  (multiply-100 n)) ; 10000
(multiply-90 (+ 10 80)) ; 8100

;; が、マクロは脳みそが混乱するのである程度慣れが必要と言える。

;; ちなみに、n-times マクロは "%%% times you've defined." のような文字列リテラルに対応していない。
;; きちんと動作させるのは意外に面倒くさい。

;; 次は外部ファイルをClojureの正式な定義に仕立て上げるマクロ。
;; CSVファイルを変数定義として自分の名前空間にインポートする。


;; とりあえず、ファイルから読み込むことは無視しよう。
;; こんな感じで呼び出せるとする。
;; リストが行ごとに分断されて要素となっている
(import-csv-vars
 ["foo,hoge" "bar,fuga" "baz,piyo"])


;; そうすると下記と同じ意味合いを持つようになるとする。
(do (def foo "hoge")
    (def bar "fuga")
    (def baz "piyo"))

;; じゃ、さっそく
(defn- csv-line-into-syms [line]
  (let [splitted (-> line
                     (.split ","))]
    [(symbol (nth splitted 0)) (nth splitted 1)]))

(csv-line-into-syms "foo,hoge") ; [foo "hoge"]

(defn import-csv-vars-form [lines]
  ;; doall付けないとmap自体が遅延シーケンスなので
  ;; with-openストリームなど期限付きの遅延シーケンス渡された場合にうまくいかない場合がある(不便…)
  ;; ここでdoallせずに与える元でdoallするのもアリ。
  `(do ~@(doall
          (map
           #(let [[varsym str] (csv-line-into-syms %)]
              `(def ~varsym ~str))
           lines))))

(defmacro import-csv-vars [lines]
  (import-csv-vars-form lines))

;; 実行！
;; (do (def foo "hoge") (def bar "fuga") (def baz "piyo")) と同じ
(import-csv-vars
 ["foo,hoge" "bar,fuga" "baz,piyo"])

(= foo "hoge") ; true

;; ファイルから読み込む機能をつけてみる
(defmacro load-vardefs-from-csv [filepath]
  (with-open [rdr (clojure.java.io/reader filepath)]
    (import-csv-vars-form (line-seq rdr))))

;; ファイルはcjpg/vardefs.csvに用意してある。
(load-vardefs-from-csv "src/cjpg/vardefs.csv")

;; 上はちゃんとこうなる
(do (def foo "hoge") (def bar "fuga") (def baz "piyo"))

;; 上記を応用して、コンパイル時の外部ファイルにしたがって
;; フォーム群を生成すれば、ソースの動的生成と等価となる。
;; (外部ファイルの例: SQLとか、Excelとか(Apache POIを使えばいい)、XMLとか、DBとか
;; HTTPなど外部ソースを元にして生成させてもいい。)

;; 当然、上記はコンパイル時に内容がすべて決定してもいいという前提での話。
;; 実行時にロードする必要があるとか、ソースとなったファイルを差し替えただけで挙動が変わって欲しいといった場合は
;; 特殊な仕掛けがない限り、他の言語同様関数として実装する必要がある。

;; コンパイル時に値を評価してしまうマクロ
(defmacro inline [expr]
  (eval expr))

(inline (+ 1 2)) ; コンパイルしたら3になる。

;; カリー化
(defn currize [[x & xs] body]
  `(fn [~x]
     ~(if xs
        (currize xs body)
        `(do ~@body))))

(defmacro currying [params & body]
  (currize params body))

;; これは
(currying [a b c d]
          (+ (* a b) (/ c d)))

;; 下記に展開される
(clojure.core/fn [a]
  (clojure.core/fn [b]
    (clojure.core/fn [c]
      (clojure.core/fn [d] (do (+ (* a b) (/ c d)))))))

;; 使ってみるけど、カッコがめんどい
((((currying [a b c] (+ a b c)) 10) 20) 30)

;; めんどいので、マクロつくる
(defn reverse-arrow [args body]
  (if (empty? args)
    body
    (recur (rest args) `(~body ~(first args)))))

(defmacro <- [subject & args]
  (reverse-arrow args subject))

;; こう書けるよ
(<- (currying [a b c] (+ a b c))
    10 20 30)
;; こうなる
((((currying [a b c] (+ a b c)) 10) 20) 30)
;; 最終的にはこれ
((((fn* ([a] (fn* ([b] (fn* ([c] (do (+ a b c)))))))) 10) 20) 30)


;; マクロの道具など

;; 当然、パターンマッチング的なのは出来る
(defmacro guess-arity
  ([] "no args")
  ([_] "one args")
  ([_ _] "two args"))

(guess-arity) ; "no args"
(guess-arity "") ; "one args"

;; 適当なシンボルの生成
(gensym) ; G_XXXXX
;; でも macrovar# 的な書き方があるので、あんまり出番ないかも

;; シンボルの置換
(clojure.template/apply-template
 '[like I] '[I like Apples!] '[hate You]) ; You hate Apples!

;; こういう用途だと思います
(clojure.template/apply-template
 '[<>]
 ;; 式中の<>をマクロなどで置き換える場合
 '(let [<> 30]
    (+ <> <>))
 [(gensym)])



