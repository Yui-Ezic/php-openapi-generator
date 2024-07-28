<?php

namespace App\QuerySerializer\Transformer\Form;

use Attribute;
use CuyZ\Valinor\Normalizer\AsTransformer;

/**
 * THIS TRANSFORMER MUST WORK IN TANDEM WITH App\QuerySerializer\Transformer\ExplodeValues
 * TODO: find other method to do object explode, without App\QuerySerializer\Transformer\ExplodeValues
 */
#[AsTransformer]
#[Attribute(Attribute::TARGET_PROPERTY)]
readonly class ObjectExplode
{
    public const string EXPLODE_FLAG = '-explode';

    /**
     * App\QuerySerializer\Transformer\ExplodeValues search for EXPLODE_FLAG keys and explode it
     */
    public function normalizeKey(string $value): string
    {
        return self::EXPLODE_FLAG;
    }
}