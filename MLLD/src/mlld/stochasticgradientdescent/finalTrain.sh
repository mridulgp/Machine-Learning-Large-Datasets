#length=`wc -l < $1`
#head -n $length $1

modeldir=$5
rm -rf $modeldir/train_data
mkdir -p $modeldir
mkdir -p $modeldir/sgd.lr
mkdir $modeldir/train_data

trainfile=`basename $4`

#for ((i=1; i<=20; i++))
for i in $(seq 1 20)
do
	shuf $4 > $modeldir/train_data/$trainfile.$i
done

length=`wc -l < $4`

java LogisticRegression $modeldir/train_data $length $1 $2 $modeldir/sgd.lr | grep "Likelihood"

#java LRTest $3 $modeldir/sgd.lr/sgd.lr.$1-$2.final $1
