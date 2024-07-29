<?php

namespace App\QuerySerializer;

use App\QuerySerializer\Query\Form;
use App\QuerySerializer\Query\SpaceDelimited;
use App\QuerySerializer\Query\StringValue;
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

$reserved = "/?#[]@!$&'()*+,;=";
$shouldBeEncoded = ' |^ёі';
$unreserved = '-._~' . 'Ab09';

$tests = [
    'allowReserved=true encode everything except reserved' => [
        'query' => new StringValue($unreserved . $reserved . $shouldBeEncoded),
        'allowReserved' => true,
        'expected' => 'value=' . $unreserved . $reserved . rawurlencode($shouldBeEncoded)
    ],
    'allowReserved=false encode everything' => [
        'query' => new StringValue($unreserved . $reserved . $shouldBeEncoded),
        'allowReserved' => false,
        'expected' => 'value=' . $unreserved . rawurlencode($reserved . $shouldBeEncoded)
    ],
    'Form, explode, no allow reserved' => [
        'query' => Form\Explode::class,
        'allowReserved' => false,
        'expected' => implode('&', [
            'int=3',
            'float=3.14',
            'string=' . 'hello' . '%20' . 'world',
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
            'string=' . 'hello' . '%20' . 'world',
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
            'string=' . 'hello' . '%20' . 'world',
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
            'string=' . 'hello' . '%20' . 'world',
            'stringList=first,second',
            'nestedObject=id,1,value,foo',
        ]),
    ],
    'SpaceDelimited, no explode, no allow reserved' => [
        'query' => SpaceDelimited\NoExplode::class,
        'allowReserved' => false,
        'expected' => implode('&', [
            'stringList=' . 'first' . '%20' . 'second',
        ]),
    ],
    'SpaceDelimited, no explode, allow reserved' => [
        'query' => SpaceDelimited\NoExplode::class,
        'allowReserved' => true,
        'expected' => implode('&', [
            'stringList=' . 'first' . '%20' . 'second',
        ]),
    ],
];

foreach ($tests as $name => $test) {
    if (is_string($test['query'])) {
        $query = (new MapperBuilder())
            ->allowSuperfluousKeys()
            ->mapper()
            ->map($test['query'], $queryArray);
    } else {
        $query = $test['query'];
    }

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
