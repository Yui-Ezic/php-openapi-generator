<?php

namespace App\QuerySerializer\Query\Form;

use App\QuerySerializer\NestedObject;
use App\QuerySerializer\Transformer\ArrayExplode;
use App\QuerySerializer\Transformer\Form\ObjectExplode;

readonly class Explode
{
    public function __construct(
        public int $int,
        public float $float,
        public string $string,
        /** @var list<string> */
        #[ArrayExplode('stringList')]
        public array $stringList,
        #[ObjectExplode]
        public NestedObject $nestedObject,
    )
    {
    }
}