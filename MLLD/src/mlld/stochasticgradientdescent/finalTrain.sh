#length=`wc -l < $1`
#head -n $length $1

rm -rf train_data
mkdir -p sgd.lr
mkdir train_data

trainfile=`basename $4`

#for ((i=1; i<=20; i++))
for i in $(seq 1 20)
do
	shuf $4 > train_data/$trainfile.$i
done

length=`wc -l < $4`

java LogisticRegression train_data/ $length $1 $2 | grep "Likelihood"

java LRTest $3 sgd.lr/sgd.lr.$1-$2.final $1
