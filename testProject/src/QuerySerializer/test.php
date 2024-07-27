<?php

namespace App\QuerySerializer;

use App\QuerySerializer\Book\BooksGETParameterData;

require __DIR__ . '/../../vendor/autoload.php';

$query = new BooksGETParameterData(
    offset: 20,
    limit: 10
);

$serializer = new QuerySerializer();

$actual = $serializer->serialize($query);
$expected = "offset=20&limit=120";
if ($actual !== $expected) {
    echo $query::class . ": failed assert that \n'$actual' \nequals to \n'$expected'" . PHP_EOL . PHP_EOL;
};

echo 'Done;';
