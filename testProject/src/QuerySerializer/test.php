<?php

namespace App\QuerySerializer;

require __DIR__ . '/../../vendor/autoload.php';

$query = new Query(
    int: 3,
    float: 3.14,
    string: 'hello world',
    stringList: ['first', 'second'],
    nestedObject: new NestedObject(
        id: 1,
        value: 'foo'
    )
);

$serializer = new QuerySerializer();

$actual = $serializer->serialize($query);
$expected = implode('&', [
    'int=3',
    'float=3.14',
    'string=' . rawurlencode('hello world'),
    'stringList=first&stringList=second',
    'id=1',
    'value=foo'
]);
$decodedActual = rawurldecode($actual);

echo $query::class . PHP_EOL;
echo '------------------------------' . PHP_EOL;

echo "Actual result (decoded): \n$decodedActual" . PHP_EOL;
echo "Actual result (original): \n$actual" . PHP_EOL;
echo PHP_EOL;

if ($actual !== $expected) {
    echo "Failed assert that equals to \n'$expected'" . PHP_EOL;
    echo PHP_EOL;
};

echo 'Done;';
