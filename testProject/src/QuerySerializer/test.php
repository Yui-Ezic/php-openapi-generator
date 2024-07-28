<?php

namespace App\QuerySerializer;

use App\QuerySerializer\Query\Form;
use CuyZ\Valinor\MapperBuilder;

require __DIR__ . '/../../vendor/autoload.php';

$queryArray = [
    'int' => 3,
    'float' => 3.14,
    'string' => 'hello world',
    'stringList' => ['first', 'second'],
    'nestedObject' => [
        'id' => 1,
        'value' => 'foo'
    ]
];

$serializer = new QuerySerializer();

$tests = [
    'Form, explode, no allow reserved' => [
        'query' => Form\Explode::class,
        'allowReserved' => false,
        'expected' => implode('&', [
            'int=3',
            'float=3.14',
            'string=' . rawurlencode('hello world'),
            'stringList=first&stringList=second',
            'id=1',
            'value=foo'
        ]),
    ],
    'Form, explode, allow reserved' => [
        'query' => Form\Explode::class,
        'allowReserved' => true,
        'expected' => implode('&', [
            'int=3',
            'float=3.14',
            'string=hello world',
            'stringList=first&stringList=second',
            'id=1',
            'value=foo'
        ]),
    ],
    'Form, no explode, no allow reserved' => [
        'query' => Form\NoExplode::class,
        'allowReserved' => false,
        'expected' => implode('&', [
            'int=3',
            'float=3.14',
            'string=' . rawurlencode('hello world'),
            'stringList=first,second',
            'nestedObject=id,1,value,foo',
        ]),
    ],
    'Form, no explode, allow reserved' => [
        'query' => Form\NoExplode::class,
        'allowReserved' => true,
        'expected' => implode('&', [
            'int=3',
            'float=3.14',
            'string=' . 'hello world',
            'stringList=first,second',
            'nestedObject=id,1,value,foo',
        ]),
    ],
];

foreach ($tests as $name => $test) {
    $query = (new MapperBuilder())->mapper()->map($test['query'], $queryArray);

    $actual = $serializer->serialize(
        query: $query,
        allowReserved: $test['allowReserved'],
    );
    $decodedActual = rawurldecode($actual);

    echo "\e[33m" . $query::class . ': ' . $name . "\e[39m" . PHP_EOL;
    echo '------------------------------' . PHP_EOL;
    echo "Actual result (decoded): \n$decodedActual" . PHP_EOL;
    echo "Actual result (original): \n$actual" . PHP_EOL;
    echo PHP_EOL;

    if ($actual !== $test['expected']) {
        echo "\e[31m" . "Failed assert that equals to \n'{$test['expected']}'" . "\e[39m" . PHP_EOL;
    } else {
        echo "\e[32m" . 'Success' . "\e[39m" . PHP_EOL;
    };

    echo PHP_EOL;
}

echo 'Done';
