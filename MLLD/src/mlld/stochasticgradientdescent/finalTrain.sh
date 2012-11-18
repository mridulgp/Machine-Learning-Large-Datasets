#length=`wc -l < $1`
#head -n $length $1

rm -rf train_data
mkdir -p sgd.lr
mkdir train_data

for ((i=1; i<=20; i++))
do
	shuf abstract.tiny.train > train_data/abstract.tiny.train.$i
done

length=`wc -l < abstract.tiny.train`

java LogisticRegression train_data/ $length $1 $2 | grep "Likelihood"

java LRTest $3 sgd.lr/sgd.lr.$1-$2.final $1
