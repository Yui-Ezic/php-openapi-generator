<?php

namespace App\QuerySerializer\Query\Form;

use App\QuerySerializer\NestedObject;
use App\QuerySerializer\Transformer\Form\ArrayNoExplode;
use App\QuerySerializer\Transformer\Form\ObjectNoExplode;

readonly class NoExplode
{
    public function __construct(
        public int $int,
        public float $float,
        public string $string,
        /** @var list<string> */
        #[ArrayNoExplode]
        public array $stringList,
        #[ObjectNoExplode]
        public NestedObject $nestedObject,
    )
    {
    }
}