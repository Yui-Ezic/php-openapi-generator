<?php

namespace App\QuerySerializer;

readonly class Query
{
    public function __construct(
        public int $int,
        public float $float,
        public string $string,
        /** @var list<string> */
        public array $stringList,
        public NestedObject $nestedObject,
    )
    {
    }
}