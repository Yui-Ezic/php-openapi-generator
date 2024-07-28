<?php

namespace App\QuerySerializer;

use App\QuerySerializer\Transformer\Form;
use App\QuerySerializer\Transformer\UrlEncode;
use CuyZ\Valinor\MapperBuilder;
use CuyZ\Valinor\Normalizer\Format;

final readonly class QuerySerializer
{
    public function serialize(object $query, bool $allowReserved = false, bool $explode = true): string
    {
        $mapperBuilder = new MapperBuilder();

        if ($allowReserved === false) {
            $mapperBuilder = $mapperBuilder->registerTransformer(new UrlEncode());
        }

        if ($explode === true) {
            $mapperBuilder = $mapperBuilder
                ->registerTransformer(new Form\ArrayExplode())
                ->registerTransformer(new Form\ObjectExplode());
        } else {
            $mapperBuilder = $mapperBuilder
                ->registerTransformer(new Form\ArrayNoExplode())
                ->registerTransformer(new Form\ObjectNoExplode());
        }

        $normalizer = $mapperBuilder->normalizer(Format::array());

        $array = $normalizer->normalize($query);

        $arrayForImplode = [];
        foreach ($array as $key => $value) {
            $arrayForImplode[$key] = $key . '=' . $value;
        }

        return implode("&", $arrayForImplode);
    }
}