<?php

namespace App\QuerySerializer;

use App\QuerySerializer\Transformer\ExplodeValues;
use App\QuerySerializer\Transformer\Form;
use App\QuerySerializer\Transformer\UrlEncode;
use CuyZ\Valinor\MapperBuilder;
use CuyZ\Valinor\Normalizer\Format;

final readonly class QuerySerializer
{
    public function serialize(object $query, bool $allowReserved = false): string
    {
        $mapperBuilder = new MapperBuilder();

        if ($allowReserved === false) {
            $mapperBuilder = $mapperBuilder->registerTransformer(new UrlEncode());
        }

        // For ObjectExplode attribute
        $mapperBuilder = $mapperBuilder->registerTransformer(new ExplodeValues());

        $normalizer = $mapperBuilder->normalizer(Format::array());

        $array = $normalizer->normalize($query);

        $arrayForImplode = [];
        foreach ($array as $key => $value) {
            $arrayForImplode[$key] = $key . '=' . $value;
        }

        return implode("&", $arrayForImplode);
    }
}